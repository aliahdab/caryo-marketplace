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
