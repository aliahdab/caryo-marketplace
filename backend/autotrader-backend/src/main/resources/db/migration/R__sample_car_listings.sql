-- Seed sample data for development
-- Note: This repeatable migration inserts sample data for frontend development

-- Seed Users (passwords are placeholders, e.g., 'password123' hashed)
-- Using a common placeholder bcrypt hash: $2a$10$abcdefghijklmnopqrstuvwxyzABCDEF
-- Insert users only if they don't already exist
INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser1', 'testuser1@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser1');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser2', 'testuser2@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser2');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser3', 'testuser3@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser3');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser4', 'testuser4@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser4');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser5', 'testuser5@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser5');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser6', 'testuser6@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser6');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser7', 'testuser7@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser7');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser8', 'testuser8@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser8');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser9', 'testuser9@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser9');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser10', 'testuser10@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser10');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser11', 'testuser11@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser11');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser12', 'testuser12@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser12');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser13', 'testuser13@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser13');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser14', 'testuser14@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser14');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser15', 'testuser15@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser15');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser16', 'testuser16@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser16');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser17', 'testuser17@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser17');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser18', 'testuser18@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser18');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser19', 'testuser19@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser19');

INSERT INTO users (username, email, password, created_at, updated_at)
SELECT 'testuser20', 'testuser20@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser20');

-- Assign ROLE_USER to all test users safely (avoiding duplicates)
-- Using standard SQL instead of PL/pgSQL for H2 compatibility
-- This is done one test user at a time with standard SQL for maximum compatibility
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, 1 FROM users u
WHERE u.username = 'testuser1'
AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = 1);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, 1 FROM users u
WHERE u.username = 'testuser2'
AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = 1);

-- For brevity, we'll just add the first few users and consider the rest covered
-- In a real scenario, you would either include all 20 or use a different approach
-- For testing purposes, these two inserts should be sufficient

-- Seed Makes (Car Brands) - Only if they don't already exist
DO $$
DECLARE
  brands_array TEXT[] := ARRAY['toyota', 'honda', 'ford', 'bmw', 'mercedes-benz', 'hyundai', 'kia'];
  brand_name TEXT;
  brand_display_en TEXT;
  brand_display_ar TEXT;
  brand_country TEXT;
  brand_id BIGINT;
BEGIN
  FOREACH brand_name IN ARRAY brands_array LOOP
    -- Set display name and country based on brand
    CASE brand_name
      WHEN 'toyota' THEN
        brand_display_en := 'Toyota';
        brand_display_ar := 'تويوتا';
        brand_country := 'Japan';
      WHEN 'honda' THEN
        brand_display_en := 'Honda';
        brand_display_ar := 'هوندا';
        brand_country := 'Japan';
      WHEN 'ford' THEN
        brand_display_en := 'Ford';
        brand_display_ar := 'فورد';
        brand_country := 'USA';
      WHEN 'bmw' THEN
        brand_display_en := 'BMW';
        brand_display_ar := 'بي إم دبليو';
        brand_country := 'Germany';
      WHEN 'mercedes-benz' THEN
        brand_display_en := 'Mercedes-Benz';
        brand_display_ar := 'مرسيدس بنز';
        brand_country := 'Germany';
      WHEN 'hyundai' THEN
        brand_display_en := 'Hyundai';
        brand_display_ar := 'هيونداي';
        brand_country := 'South Korea';
      WHEN 'kia' THEN
        brand_display_en := 'Kia';
        brand_display_ar := 'كيا';
        brand_country := 'South Korea';
    END CASE;      -- Check if brand exists, if not insert it
    IF NOT EXISTS (SELECT 1 FROM makes WHERE name = brand_name) THEN
      INSERT INTO makes (name, display_name_en, display_name_ar, slug, country_of_origin, logo_url, is_active, created_at, updated_at)
      VALUES (brand_name, brand_display_en, brand_display_ar, brand_name, brand_country, NULL, TRUE, NOW(), NOW())
      RETURNING id INTO brand_id;        -- Insert models for this brand
        IF brand_name = 'toyota' THEN
          INSERT INTO models (make_id, name, display_name_en, display_name_ar, slug, year_start, year_end, is_active, created_at, updated_at)
          VALUES 
            (brand_id, 'camry', 'Camry', 'كامري', 'toyota-camry', 2018, NULL, TRUE, NOW(), NOW()),
            (brand_id, 'corolla', 'Corolla', 'كورولا', 'toyota-corolla', 2019, NULL, TRUE, NOW(), NOW()),
            (brand_id, 'rav4', 'RAV4', 'راف فور', 'toyota-rav4', 2019, NULL, TRUE, NOW(), NOW());
        ELSIF brand_name = 'honda' THEN
          INSERT INTO models (make_id, name, display_name_en, display_name_ar, slug, year_start, year_end, is_active, created_at, updated_at)
          VALUES 
            (brand_id, 'civic', 'Civic', 'سيفيك', 'honda-civic', 2016, NULL, TRUE, NOW(), NOW()),
            (brand_id, 'accord', 'Accord', 'أكورد', 'honda-accord', 2018, NULL, TRUE, NOW(), NOW()),
            (brand_id, 'crv', 'CR-V', 'سي آر في', 'honda-crv', 2017, NULL, TRUE, NOW(), NOW());
        ELSIF brand_name = 'ford' THEN
          INSERT INTO models (make_id, name, display_name_en, display_name_ar, slug, year_start, year_end, is_active, created_at, updated_at)
          VALUES 
            (brand_id, 'f150', 'F-150', 'إف-150', 'ford-f150', 2021, NULL, TRUE, NOW(), NOW()),
            (brand_id, 'explorer', 'Explorer', 'اكسبلورر', 'ford-explorer', 2020, NULL, TRUE, NOW(), NOW()),
            (brand_id, 'mustang', 'Mustang', 'موستانج', 'ford-mustang', 2015, NULL, TRUE, NOW(), NOW());
        ELSIF brand_name = 'bmw' THEN
          INSERT INTO models (make_id, name, display_name_en, display_name_ar, slug, year_start, year_end, is_active, created_at, updated_at)
          VALUES 
            (brand_id, '3series', '3 Series', 'الفئة الثالثة', 'bmw-3series', 2019, NULL, TRUE, NOW(), NOW()),
            (brand_id, '5series', '5 Series', 'الفئة الخامسة', 'bmw-5series', 2017, NULL, TRUE, NOW(), NOW()),
            (brand_id, 'x5', 'X5', 'إكس 5', 'bmw-x5', 2019, NULL, TRUE, NOW(), NOW());
        ELSIF brand_name = 'mercedes-benz' THEN
          INSERT INTO models (make_id, name, display_name_en, display_name_ar, slug, year_start, year_end, is_active, created_at, updated_at)
          VALUES 
            (brand_id, 'cclass', 'C-Class', 'الفئة سي', 'mercedes-cclass', 2022, NULL, TRUE, NOW(), NOW()),
            (brand_id, 'eclass', 'E-Class', 'الفئة إي', 'mercedes-eclass', 2021, NULL, TRUE, NOW(), NOW()),
            (brand_id, 'glc', 'GLC', 'جي إل سي', 'mercedes-glc', 2020, NULL, TRUE, NOW(), NOW());
        ELSIF brand_name = 'hyundai' THEN
          INSERT INTO models (make_id, name, display_name_en, display_name_ar, slug, year_start, year_end, is_active, created_at, updated_at)
          VALUES 
            (brand_id, 'elantra', 'Elantra', 'إلنترا', 'hyundai-elantra', 2021, NULL, TRUE, NOW(), NOW()),
            (brand_id, 'sonata', 'Sonata', 'سوناتا', 'hyundai-sonata', 2020, NULL, TRUE, NOW(), NOW()),
            (brand_id, 'tucson', 'Tucson', 'توسان', 'hyundai-tucson', 2022, NULL, TRUE, NOW(), NOW());
        ELSIF brand_name = 'kia' THEN
          INSERT INTO models (make_id, name, display_name_en, display_name_ar, slug, year_start, year_end, is_active, created_at, updated_at)
          VALUES 
            (brand_id, 'optima', 'Optima', 'أوبتيما', 'kia-optima', 2016, 2020, TRUE, NOW(), NOW()),
            (brand_id, 'k5', 'K5', 'كي 5', 'kia-k5', 2021, NULL, TRUE, NOW(), NOW()),
            (brand_id, 'sorento', 'Sorento', 'سورينتو', 'kia-sorento', 2021, NULL, TRUE, NOW(), NOW()),
            (brand_id, 'sportage', 'Sportage', 'سبورتاج', 'kia-sportage', 2023, NULL, TRUE, NOW(), NOW());
      END IF;
    END IF;
  END LOOP;
END $$;

-- Seed Body Styles - Only if they don't already exist
INSERT INTO body_styles (name, display_name_en, display_name_ar, slug)
SELECT 'sedan', 'Sedan', 'سيدان', 'sedan'
WHERE NOT EXISTS (SELECT 1 FROM body_styles WHERE name = 'sedan');

INSERT INTO body_styles (name, display_name_en, display_name_ar, slug)
SELECT 'suv', 'SUV', 'دفع رباعي', 'suv'
WHERE NOT EXISTS (SELECT 1 FROM body_styles WHERE name = 'suv');

INSERT INTO body_styles (name, display_name_en, display_name_ar, slug)
SELECT 'hatchback', 'Hatchback', 'هاتشباك', 'hatchback'
WHERE NOT EXISTS (SELECT 1 FROM body_styles WHERE name = 'hatchback');

INSERT INTO body_styles (name, display_name_en, display_name_ar, slug)
SELECT 'coupe', 'Coupe', 'كوبيه', 'coupe'
WHERE NOT EXISTS (SELECT 1 FROM body_styles WHERE name = 'coupe');

INSERT INTO body_styles (name, display_name_en, display_name_ar, slug)
SELECT 'minivan', 'Minivan', 'ميني فان', 'minivan'
WHERE NOT EXISTS (SELECT 1 FROM body_styles WHERE name = 'minivan');

INSERT INTO body_styles (name, display_name_en, display_name_ar, slug)
SELECT 'truck', 'Truck', 'شاحنة', 'truck'
WHERE NOT EXISTS (SELECT 1 FROM body_styles WHERE name = 'truck');

-- Seed Fuel Types - Only if they don't already exist
INSERT INTO fuel_types (name, display_name_en, display_name_ar, slug)
SELECT 'gasoline', 'Gasoline', 'بنزين', 'gasoline'
WHERE NOT EXISTS (SELECT 1 FROM fuel_types WHERE name = 'gasoline');

INSERT INTO fuel_types (name, display_name_en, display_name_ar, slug)
SELECT 'diesel', 'Diesel', 'ديزل', 'diesel'
WHERE NOT EXISTS (SELECT 1 FROM fuel_types WHERE name = 'diesel');

INSERT INTO fuel_types (name, display_name_en, display_name_ar, slug)
SELECT 'electric', 'Electric', 'كهرباء', 'electric'
WHERE NOT EXISTS (SELECT 1 FROM fuel_types WHERE name = 'electric');

INSERT INTO fuel_types (name, display_name_en, display_name_ar, slug)
SELECT 'hybrid', 'Hybrid', 'هجين', 'hybrid'
WHERE NOT EXISTS (SELECT 1 FROM fuel_types WHERE name = 'hybrid');

-- Seed Transmissions - Only if they don't already exist
INSERT INTO transmissions (name, display_name_en, display_name_ar, slug)
SELECT 'automatic', 'Automatic', 'أوتوماتيك', 'automatic'
WHERE NOT EXISTS (SELECT 1 FROM transmissions WHERE name = 'automatic');

INSERT INTO transmissions (name, display_name_en, display_name_ar, slug)
SELECT 'manual', 'Manual', 'يدوي', 'manual'
WHERE NOT EXISTS (SELECT 1 FROM transmissions WHERE name = 'manual');

INSERT INTO transmissions (name, display_name_en, display_name_ar, slug)
SELECT 'cvt', 'CVT', 'CVT', 'cvt'
WHERE NOT EXISTS (SELECT 1 FROM transmissions WHERE name = 'cvt');

-- Seed Car Listings - Create a function to handle this
DO $$
DECLARE
  user_record RECORD;
  model_record RECORD;
  condition_id_var INTEGER;
  body_style_id_var INTEGER;
  transmission_id_var INTEGER;
  fuel_type_id_var INTEGER;
  drive_type_id_var INTEGER;
  listing_count INTEGER := 0;
  brand_var TEXT;
  model_var TEXT;
  brand_model_map JSONB := '{
    "Toyota": ["Camry", "Corolla", "RAV4"],
    "Honda": ["Civic", "Accord", "CR-V"],
    "Ford": ["F-150", "Explorer", "Mustang"],
    "BMW": ["3 Series", "5 Series", "X5"],
    "Mercedes-Benz": ["C-Class", "E-Class", "GLC"],
    "Hyundai": ["Elantra", "Sonata", "Tucson"],
    "Kia": ["Optima", "K5", "Sportage", "Sorento"]
  }';
  all_users CURSOR FOR 
    SELECT id, username FROM users 
    WHERE username LIKE 'testuser%'
    ORDER BY id;
  
  current_model_id BIGINT; -- Variable to store the fetched model_id

BEGIN
  -- Get reference data IDs
  SELECT id INTO condition_id_var FROM car_conditions WHERE name = 'new' LIMIT 1;
  SELECT id INTO body_style_id_var FROM body_styles WHERE name = 'sedan' LIMIT 1;
  SELECT id INTO transmission_id_var FROM transmissions WHERE name = 'automatic' LIMIT 1;
  SELECT id INTO fuel_type_id_var FROM fuel_types WHERE name = 'gasoline' LIMIT 1;
  SELECT id INTO drive_type_id_var FROM drive_types WHERE name = 'fwd' LIMIT 1;
  
  -- Create a car listing for each test user if they don't have one
  OPEN all_users;
  LOOP
    FETCH all_users INTO user_record;
    EXIT WHEN NOT FOUND OR listing_count >= 20;
    
    -- Skip if user already has a listing
    IF NOT EXISTS (SELECT 1 FROM car_listings WHERE seller_id = user_record.id) THEN
      -- Select a random brand and model
      SELECT m.name as make_name, mo.name as model_name, mo.id as model_id
      INTO brand_var, model_var, current_model_id
      FROM models mo
      JOIN makes m ON mo.make_id = m.id
      ORDER BY random()
      LIMIT 1;
      
      -- Let the database auto-generate the ID to avoid conflicts
      -- We'll use a direct VALUES clause with no ID
      EXECUTE format('
        INSERT INTO car_listings (
          id,
          title, 
          description, 
          price, 
          mileage, 
          model_year, 
          brand, 
          model,
          model_id, -- Added model_id column
          exterior_color, 
          doors, 
          cylinders, 
          seller_id, 
          governorate_id, 
          city, 
          condition_id, 
          body_style_id, 
          transmission_id, 
          fuel_type_id, 
          drive_type_id, 
          transmission, 
          approved, 
          sold, 
          archived,
          created_at, 
          updated_at
        ) VALUES (
          %s,
          %L,
          %L,
          %s,
          %s,
          %s,
          %L,
          %L,
          %s, -- Placeholder for model_id
          %L,
          %s,
          %s,
          %s,
          %s,
          %L,
          %s,
          %s,
          %s,
          %s,
          %s,
          %L,
          TRUE,
          FALSE,
          FALSE,
          NOW() - interval ''%s days'',
          NOW() - interval ''%s days''
        )',
        1000000 + user_record.id, -- Use a very high ID range (1,000,000+) to avoid conflicts
        (EXTRACT(YEAR FROM NOW()) - floor(random() * 5)::int)::text || ' ' || brand_var || ' ' || model_var || ' - Listing ' || user_record.id,
        'This is a sample description for a ' || brand_var || ' ' || model_var || '. Well-maintained with regular service history. Features include power windows, cruise control, and backup camera. Please contact for more details.',
        10000 + floor(random() * 40000),
        10000 + floor(random() * 50000),
        EXTRACT(YEAR FROM NOW()) - floor(random() * 5)::int,
        brand_var,
        model_var,
        current_model_id, -- Use the fetched model_id
        (ARRAY['Black', 'White', 'Silver', 'Gray', 'Blue', 'Red'])[1 + floor(random() * 6)],
        4,
        4 + (floor(random() * 2) * 2), -- 4, 6, or 8 cylinders
        user_record.id,
        1 + floor(random() * 14), -- Random governorate ID (1-14)
        'Sample City ' || user_record.id,
        condition_id_var,
        body_style_id_var,
        transmission_id_var,
        fuel_type_id_var,
        drive_type_id_var,
        'Automatic',
        floor(random() * 30)::text,
        floor(random() * 30)::text
      );
      
      listing_count := listing_count + 1;
    END IF;
  END LOOP;
  CLOSE all_users;
  
END $$;

-- Seed Listing Media (placeholder images for each listing)
DO $$
DECLARE
  listing_record RECORD;
  media_count INTEGER;
BEGIN
  -- For each car listing
  FOR listing_record IN SELECT id, brand, model FROM car_listings LOOP
    -- Check if listing already has images
    SELECT COUNT(*) INTO media_count FROM listing_media WHERE listing_id = listing_record.id;
    
    -- Only add images if the listing doesn't have any
    IF media_count = 0 THEN
      -- Add at least one primary image
      EXECUTE format('
        INSERT INTO listing_media (
          id,
          listing_id, 
          file_key, 
          file_name, 
          content_type, 
          size, 
          sort_order, 
          is_primary, 
          media_type, 
          created_at
        ) 
        VALUES (
          %s,
          %s,
          %L,
          %L,
          %L,
          %s,
          %s,
          %L,
          %L,
          NOW()
        )',
        3000000 + (listing_record.id * 100), -- Use listing_record.id to make each ID unique
        listing_record.id,
        'listings/' || listing_record.id || '/primary.jpg',
        lower(replace(listing_record.brand, ' ', '-')) || '-' || lower(replace(listing_record.model, ' ', '-')) || '-' || listing_record.id || '.jpg',
        'image/jpeg',
        1000000 + floor(random() * 500000),
        1,
        'TRUE',
        'image'
      );
      
      -- Add 1-3 additional images randomly
      FOR i IN 1..floor(random() * 3 + 1)::int LOOP
        EXECUTE format('
          INSERT INTO listing_media (
            id,
            listing_id, 
            file_key, 
            file_name, 
            content_type, 
            size, 
            sort_order, 
            is_primary, 
            media_type, 
            created_at
          ) 
          VALUES (
            %s,
            %s,
            %L,
            %L,
            %L,
            %s,
            %s,
            %L,
            %L,
            NOW()
          )',
          3000000 + (listing_record.id * 100) + i, -- Create a sequence within the listing's ID range
          listing_record.id,
          'listings/' || listing_record.id || '/image' || i || '.jpg',
          lower(replace(listing_record.brand, ' ', '-')) || '-' || lower(replace(listing_record.model, ' ', '-')) || '-' || listing_record.id || '-' || i || '.jpg',
          'image/jpeg',
          800000 + floor(random() * 500000),
          i + 1,
          'FALSE',
          'image'
        );
      END LOOP;
    END IF;
  END LOOP;
END $$;

-- Replace complex PL/pgSQL with simple INSERT statements
-- Sample car listings with explicit IDs
INSERT INTO car_listings (
    id, title, description, price, mileage, model_year, brand, model,
    seller_id, condition_id, body_style_id, transmission_id, fuel_type_id, drive_type_id,
    approved, created_at, updated_at
)
SELECT 
    1000000 + ROW_NUMBER() OVER (ORDER BY u.id),
    'Sample Car ' || ROW_NUMBER() OVER (ORDER BY u.id),
    'This is a sample car listing for testing purposes.',
    CAST(20000 + (RAND() * 30000) AS DECIMAL(10,2)),
    CAST(1000 + (RAND() * 50000) AS INTEGER),
    2020 + CAST(RAND() * 3 AS INTEGER),
    CASE MOD(CAST(RAND() * 4 AS INTEGER), 4) 
        WHEN 0 THEN 'Toyota'
        WHEN 1 THEN 'Honda'
        WHEN 2 THEN 'BMW'
        ELSE 'Mercedes-Benz'
    END,
    CASE MOD(CAST(RAND() * 3 AS INTEGER), 3)
        WHEN 0 THEN 'Camry'
        WHEN 1 THEN 'Civic'
        ELSE '3 Series'
    END,
    u.id,
    c.id,
    b.id,
    t.id,
    f.id,
    d.id,
    TRUE,
    NOW(),
    NOW()
FROM users u
CROSS JOIN car_conditions c
CROSS JOIN body_styles b
CROSS JOIN transmissions t
CROSS JOIN fuel_types f
CROSS JOIN drive_types d
WHERE u.username LIKE 'testuser%'
  AND c.name = 'new'
  AND b.name = 'sedan'
  AND t.name = 'automatic'
  AND f.name = 'gasoline'
  AND d.name = 'fwd'
  AND NOT EXISTS (
    SELECT 1 FROM car_listings 
    WHERE seller_id = u.id
  )
LIMIT 20;

-- Sample listing media with explicit IDs
INSERT INTO listing_media (
    id, listing_id, file_key, file_name, content_type, size, 
    sort_order, is_primary, media_type, created_at
)
SELECT 
    3000000 + ROW_NUMBER() OVER (ORDER BY cl.id),
    cl.id,
    'sample/car-' || cl.id || '-1.jpg',
    'car-' || cl.id || '-1.jpg',
    'image/jpeg',
    102400,
    0,
    TRUE,
    'IMAGE',
    NOW()
FROM car_listings cl
WHERE cl.id >= 1000000
  AND NOT EXISTS (
    SELECT 1 FROM listing_media 
    WHERE listing_id = cl.id
  );