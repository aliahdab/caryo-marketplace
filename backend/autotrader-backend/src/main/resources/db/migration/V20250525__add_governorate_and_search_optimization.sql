-- V20250525__add_governorate_and_search_optimization.sql

-- Create governorates table
CREATE TABLE governorates (
    id SERIAL PRIMARY KEY,
    display_name_en VARCHAR(100) NOT NULL,
    display_name_ar VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    country_code CHAR(2) NOT NULL,
    region VARCHAR(100),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for governorates table
CREATE INDEX idx_governorate_slug ON governorates(slug);
CREATE INDEX idx_governorate_country_code ON governorates(country_code);
CREATE INDEX idx_governorate_display_name_en ON governorates(display_name_en);
CREATE INDEX idx_governorate_display_name_ar ON governorates(display_name_ar);

-- Add new columns to car_listings table for the denormalized fields
ALTER TABLE car_listings 
  ADD COLUMN governorate_id BIGINT REFERENCES governorates(id),
  ADD COLUMN governorate_name_en VARCHAR(100),
  ADD COLUMN governorate_name_ar VARCHAR(100),
  ADD COLUMN brand_name_en VARCHAR(100),
  ADD COLUMN brand_name_ar VARCHAR(100),
  ADD COLUMN model_name_en VARCHAR(100),
  ADD COLUMN model_name_ar VARCHAR(100);

-- Create indexes for quick search
CREATE INDEX idx_car_listings_brand_name_en ON car_listings(brand_name_en);
CREATE INDEX idx_car_listings_brand_name_ar ON car_listings(brand_name_ar);
CREATE INDEX idx_car_listings_model_name_en ON car_listings(model_name_en);
CREATE INDEX idx_car_listings_model_name_ar ON car_listings(model_name_ar);
CREATE INDEX idx_car_listings_governorate_name_en ON car_listings(governorate_name_en);
CREATE INDEX idx_car_listings_governorate_name_ar ON car_listings(governorate_name_ar);
CREATE INDEX idx_car_listings_governorate_id ON car_listings(governorate_id);

-- Combined indexes for common search patterns
CREATE INDEX idx_car_listings_brand_model_en ON car_listings(brand_name_en, model_name_en);
CREATE INDEX idx_car_listings_brand_model_ar ON car_listings(brand_name_ar, model_name_ar);
CREATE INDEX idx_car_listings_brand_governorate_en ON car_listings(brand_name_en, governorate_name_en);
CREATE INDEX idx_car_listings_brand_governorate_ar ON car_listings(brand_name_ar, governorate_name_ar);

-- Create a function to update the denormalized fields
CREATE OR REPLACE FUNCTION update_car_listing_denormalized_fields()
RETURNS TRIGGER AS $$
BEGIN
    -- Update brand and model names (would be populated from a brands and models table in a real implementation)
    -- For now, just copy the values from the base fields
    NEW.brand_name_en = NEW.brand;
    NEW.brand_name_ar = NEW.brand; -- In a real implementation, we'd get this from a localized source
    NEW.model_name_en = NEW.model;
    NEW.model_name_ar = NEW.model; -- In a real implementation, we'd get this from a localized source
    
    -- Update governorate names if governorate is set
    IF NEW.governorate_id IS NOT NULL THEN
        SELECT 
            g.display_name_en, 
            g.display_name_ar 
        INTO 
            NEW.governorate_name_en, 
            NEW.governorate_name_ar
        FROM 
            governorates g 
        WHERE 
            g.id = NEW.governorate_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to update denormalized fields before insert or update
CREATE TRIGGER car_listing_before_insert_update
BEFORE INSERT OR UPDATE ON car_listings
FOR EACH ROW
EXECUTE FUNCTION update_car_listing_denormalized_fields();

-- Create a function to handle updates to governorates
CREATE OR REPLACE FUNCTION update_car_listings_on_governorate_change()
RETURNS TRIGGER AS $$
BEGIN
    -- Only proceed if the display names have changed
    IF (OLD.display_name_en != NEW.display_name_en OR OLD.display_name_ar != NEW.display_name_ar) THEN
        -- Update all car listings that reference this governorate
        UPDATE car_listings
        SET 
            governorate_name_en = NEW.display_name_en,
            governorate_name_ar = NEW.display_name_ar
        WHERE
            governorate_id = NEW.id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to update car listings when a governorate changes
CREATE TRIGGER governorate_after_update
AFTER UPDATE ON governorates
FOR EACH ROW
EXECUTE FUNCTION update_car_listings_on_governorate_change();

-- Create a full text search index for text search capabilities
-- This will enable more advanced search capabilities in the future
ALTER TABLE car_listings ADD COLUMN search_vector tsvector;

CREATE OR REPLACE FUNCTION car_listings_search_vector_update()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector := 
        setweight(to_tsvector('english', COALESCE(NEW.title, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.brand, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.model, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.governorate_name_en, '')), 'C') ||
        setweight(to_tsvector('english', COALESCE(NEW.description, '')), 'D');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER car_listings_search_vector_update_trigger
BEFORE INSERT OR UPDATE ON car_listings
FOR EACH ROW
EXECUTE FUNCTION car_listings_search_vector_update();

CREATE INDEX idx_car_listings_search_vector ON car_listings USING GIN(search_vector);

-- Seed initial governorates data for Syria
INSERT INTO governorates 
    (display_name_en, display_name_ar, slug, country_code, region, latitude, longitude, is_active)
VALUES
    ('Damascus', 'دمشق', 'damascus', 'SY', 'Southern Syria', 33.5138, 36.2765, true),
    ('Aleppo', 'حلب', 'aleppo', 'SY', 'Northern Syria', 36.2021, 37.1343, true),
    ('Homs', 'حمص', 'homs', 'SY', 'Central Syria', 34.7324, 36.7137, true),
    ('Latakia', 'اللاذقية', 'latakia', 'SY', 'Coastal Syria', 35.5317, 35.7902, true),
    ('Hama', 'حماة', 'hama', 'SY', 'Central Syria', 35.1332, 36.7568, true),
    ('Tartus', 'طرطوس', 'tartus', 'SY', 'Coastal Syria', 34.8889, 35.8866, true),
    ('Idlib', 'إدلب', 'idlib', 'SY', 'Northern Syria', 35.9306, 36.6339, true),
    ('Daraa', 'درعا', 'daraa', 'SY', 'Southern Syria', 32.6189, 36.1055, true),
    ('Deir ez-Zor', 'دير الزور', 'deir-ez-zor', 'SY', 'Eastern Syria', 35.3359, 40.1408, true),
    ('Al-Hasakah', 'الحسكة', 'al-hasakah', 'SY', 'Northeastern Syria', 36.5024, 40.7563, true),
    ('Raqqa', 'الرقة', 'raqqa', 'SY', 'Northern Syria', 35.9528, 39.0089, true),
    ('Al-Suwayda', 'السويداء', 'al-suwayda', 'SY', 'Southern Syria', 32.7094, 36.5658, true),
    ('Quneitra', 'القنيطرة', 'quneitra', 'SY', 'Southwestern Syria', 33.1254, 35.8240, true),
    ('Damascus Countryside', 'ريف دمشق', 'damascus-countryside', 'SY', 'Southern Syria', 33.5138, 36.2765, true);

-- Update existing car listings with initial values for denormalized fields
UPDATE car_listings 
SET 
    brand_name_en = brand,
    brand_name_ar = brand,
    model_name_en = model,
    model_name_ar = model;

-- Create a stats table for search analytics (for future use)
CREATE TABLE search_stats (
    id SERIAL PRIMARY KEY,
    search_term VARCHAR(255),
    search_type VARCHAR(50), -- 'quick', 'brand', 'model', 'governorate'
    language VARCHAR(10),
    governorate_id BIGINT REFERENCES governorates(id),
    search_count INT DEFAULT 1,
    last_searched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_search_stats_search_term ON search_stats(search_term);
CREATE INDEX idx_search_stats_search_type ON search_stats(search_type);
CREATE INDEX idx_search_stats_search_count ON search_stats(search_count DESC);

-- Create a function to log search statistics
CREATE OR REPLACE FUNCTION log_search_stats(
    p_search_term VARCHAR(255),
    p_search_type VARCHAR(50),
    p_language VARCHAR(10),
    p_governorate_id BIGINT DEFAULT NULL
)
RETURNS VOID AS $$
BEGIN
    -- Try to update existing record
    UPDATE search_stats
    SET 
        search_count = search_count + 1,
        last_searched_at = CURRENT_TIMESTAMP
    WHERE 
        search_term = p_search_term
        AND search_type = p_search_type
        AND language = p_language
        AND (
            (governorate_id IS NULL AND p_governorate_id IS NULL)
            OR governorate_id = p_governorate_id
        );
    
    -- If no record was updated, insert a new one
    IF NOT FOUND THEN
        INSERT INTO search_stats 
            (search_term, search_type, language, governorate_id)
        VALUES 
            (p_search_term, p_search_type, p_language, p_governorate_id);
    END IF;
END;
$$ LANGUAGE plpgsql;
