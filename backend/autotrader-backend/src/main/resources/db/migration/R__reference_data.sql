-- Migration R: reference_data
-- Created: 2025-05-22
-- Updated: 2025-05-29 - Fixed for PostgreSQL 14 compatibility

-- Car Conditions
INSERT INTO car_conditions (name, display_name_en, display_name_ar, slug) 
VALUES ('new', 'New', 'جديد', 'new')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO car_conditions (name, display_name_en, display_name_ar, slug) 
VALUES ('like_new', 'Like New', 'شبه جديد', 'like-new')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO car_conditions (name, display_name_en, display_name_ar, slug) 
VALUES ('excellent', 'Excellent', 'ممتاز', 'excellent')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO car_conditions (name, display_name_en, display_name_ar, slug) 
VALUES ('very_good', 'Very Good', 'جيد جداً', 'very-good')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO car_conditions (name, display_name_en, display_name_ar, slug) 
VALUES ('good', 'Good', 'جيد', 'good')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO car_conditions (name, display_name_en, display_name_ar, slug) 
VALUES ('fair', 'Fair', 'مقبول', 'fair')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

-- Drive Types
INSERT INTO drive_types (name, display_name_en, display_name_ar, slug) 
VALUES ('fwd', 'Front-Wheel Drive', 'دفع أمامي', 'fwd')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO drive_types (name, display_name_en, display_name_ar, slug) 
VALUES ('rwd', 'Rear-Wheel Drive', 'دفع خلفي', 'rwd')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO drive_types (name, display_name_en, display_name_ar, slug) 
VALUES ('awd', 'All-Wheel Drive', 'دفع رباعي', 'awd')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO drive_types (name, display_name_en, display_name_ar, slug) 
VALUES ('4wd', 'Four-Wheel Drive', 'دفع رباعي', '4wd')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

-- Body Styles
INSERT INTO body_styles (name, display_name_en, display_name_ar, slug) 
VALUES ('sedan', 'Sedan', 'سيدان', 'sedan')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO body_styles (name, display_name_en, display_name_ar, slug) 
VALUES ('suv', 'SUV', 'إس يو في', 'suv')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO body_styles (name, display_name_en, display_name_ar, slug) 
VALUES ('hatchback', 'Hatchback', 'هاتشباك', 'hatchback')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO body_styles (name, display_name_en, display_name_ar, slug) 
VALUES ('coupe', 'Coupe', 'كوبيه', 'coupe')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO body_styles (name, display_name_en, display_name_ar, slug) 
VALUES ('pickup', 'Pickup Truck', 'بيك أب', 'pickup')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO body_styles (name, display_name_en, display_name_ar, slug) 
VALUES ('convertible', 'Convertible', 'مكشوفة', 'convertible')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO body_styles (name, display_name_en, display_name_ar, slug) 
VALUES ('wagon', 'Wagon', 'ستيشن', 'wagon')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO body_styles (name, display_name_en, display_name_ar, slug) 
VALUES ('van', 'Van', 'فان', 'van')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO body_styles (name, display_name_en, display_name_ar, slug) 
VALUES ('minivan', 'Minivan', 'ميني فان', 'minivan')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO body_styles (name, display_name_en, display_name_ar, slug) 
VALUES ('crossover', 'Crossover', 'كروس أوفر', 'crossover')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

-- Fuel Types
INSERT INTO fuel_types (name, display_name_en, display_name_ar, slug) 
VALUES ('gasoline', 'Gasoline', 'بنزين', 'gasoline')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO fuel_types (name, display_name_en, display_name_ar, slug) 
VALUES ('diesel', 'Diesel', 'ديزل', 'diesel')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO fuel_types (name, display_name_en, display_name_ar, slug) 
VALUES ('hybrid', 'Hybrid', 'هجين', 'hybrid')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO fuel_types (name, display_name_en, display_name_ar, slug) 
VALUES ('electric', 'Electric', 'كهرباء', 'electric')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO fuel_types (name, display_name_en, display_name_ar, slug) 
VALUES ('cng', 'CNG', 'غاز طبيعي', 'cng')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO fuel_types (name, display_name_en, display_name_ar, slug) 
VALUES ('lpg', 'LPG', 'غاز مسال', 'lpg')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

-- Transmissions
INSERT INTO transmissions (name, display_name_en, display_name_ar, slug) 
VALUES ('automatic', 'Automatic', 'أوتوماتيك', 'automatic')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO transmissions (name, display_name_en, display_name_ar, slug) 
VALUES ('manual', 'Manual', 'عادي', 'manual')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO transmissions (name, display_name_en, display_name_ar, slug) 
VALUES ('cvt', 'CVT', 'تعشيق مستمر', 'cvt')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO transmissions (name, display_name_en, display_name_ar, slug) 
VALUES ('semi_auto', 'Semi-Automatic', 'نصف أوتوماتيك', 'semi-auto')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO transmissions (name, display_name_en, display_name_ar, slug) 
VALUES ('dual_clutch', 'Dual Clutch', 'ثنائي القابض', 'dual-clutch')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

-- Seller Types
CREATE TABLE IF NOT EXISTS seller_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL,
    display_name_en VARCHAR(50) NOT NULL,
    display_name_ar VARCHAR(50) NOT NULL,
    slug VARCHAR(50) NOT NULL
);

INSERT INTO seller_types (name, display_name_en, display_name_ar, slug) 
VALUES ('private', 'Private Seller', 'بائع خاص', 'private')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO seller_types (name, display_name_en, display_name_ar, slug) 
VALUES ('dealer', 'Dealer', 'معرض سيارات', 'dealer')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;

INSERT INTO seller_types (name, display_name_en, display_name_ar, slug) 
VALUES ('certified', 'Certified Dealer', 'معرض معتمد', 'certified')
ON CONFLICT (name) DO UPDATE SET 
    display_name_en = EXCLUDED.display_name_en, 
    display_name_ar = EXCLUDED.display_name_ar, 
    slug = EXCLUDED.slug;
