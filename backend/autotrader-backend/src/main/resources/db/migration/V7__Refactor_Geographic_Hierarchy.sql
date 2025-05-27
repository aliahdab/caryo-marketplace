-- Create the new countries table
CREATE TABLE countries (
    id BIGSERIAL PRIMARY KEY,
    country_code VARCHAR(2) NOT NULL UNIQUE,
    display_name_en VARCHAR(255) NOT NULL,
    display_name_ar VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Insert Syria as the default country
INSERT INTO countries (country_code, display_name_en, display_name_ar, is_active)
VALUES ('SY', 'Syria', 'سوريا', TRUE);

-- Alter governorates table
ALTER TABLE governorates
ADD COLUMN country_id BIGINT,
ADD CONSTRAINT fk_governorates_country
    FOREIGN KEY (country_id)
    REFERENCES countries(id);

-- Update existing governorates to link to the default country (Kuwait)
-- This assumes all existing governorates belong to 'KW'
UPDATE governorates g
SET country_id = (SELECT c.id FROM countries c WHERE c.country_code = 'KW')
WHERE g.country_code = 'KW';

-- It's safer to make country_id NOT NULL after populating it.
-- However, if there are governorates with country_codes other than 'KW' that were not migrated,
-- this will fail. For this script, we assume 'KW' is the only one or others are handled.
ALTER TABLE governorates
ALTER COLUMN country_id SET NOT NULL;

-- Drop the old country_code column from governorates
ALTER TABLE governorates
DROP COLUMN country_code;

-- Alter locations table
ALTER TABLE locations
ADD COLUMN governorate_id BIGINT,
ADD CONSTRAINT fk_locations_governorate
    FOREIGN KEY (governorate_id)
    REFERENCES governorates(id);

-- Note: Populating locations.governorate_id is complex and depends on existing data relationships.
-- This script does not automatically populate locations.governorate_id.
-- This will require a separate data migration effort based on specific business rules
-- or manual updates if the dataset is small.
-- For new listings, the application logic will ensure governorate_id is set.

-- Drop the old country_code column from locations
-- This is safe only if locations are always accessed via governorate -> country
-- or if the application logic is updated to no longer rely on locations.country_code directly.
-- Given the new hierarchy, this should be the case.
ALTER TABLE locations
DROP COLUMN country_code;


-- Update created_at and updated_at for existing tables if they don't exist
-- For governorates
ALTER TABLE governorates
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- For locations
ALTER TABLE locations
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- Update existing rows to have a default value for created_at and updated_at if they are NULL
UPDATE governorates SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE governorates SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
UPDATE locations SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE locations SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;

