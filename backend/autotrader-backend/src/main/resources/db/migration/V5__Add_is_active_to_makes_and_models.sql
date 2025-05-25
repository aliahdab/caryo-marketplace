-- V5__Add_is_active_to_makes_and_models.sql
-- Adds the is_active column to the makes and models tables.

ALTER TABLE makes
ADD COLUMN is_active BOOLEAN DEFAULT TRUE NOT NULL;

ALTER TABLE models
ADD COLUMN is_active BOOLEAN DEFAULT TRUE NOT NULL;
