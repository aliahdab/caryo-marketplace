# Database Schema

This document outlines the database schema for the AutoTrader Marketplace backend, reflecting the implemented Java entities.

## Core Tables

### Table: users
- **id**: BIGINT PRIMARY KEY
- **username**: VARCHAR(50) UNIQUE NOT NULL
- **email**: VARCHAR(50) UNIQUE NOT NULL
- **password**: VARCHAR(120) NOT NULL
- **created_at**: TIMESTAMP NOT NULL
- **updated_at**: TIMESTAMP NOT NULL

### Table: roles
- **id**: INTEGER PRIMARY KEY
- **name**: VARCHAR(20) UNIQUE NOT NULL (e.g., ROLE_USER, ROLE_ADMIN)

### Table: user_roles
- **user_id**: BIGINT (Foreign Key to users.id)
- **role_id**: INTEGER (Foreign Key to roles.id)
- PRIMARY KEY (user_id, role_id)

## Location Tables

### Table: locations
- **id**: BIGINT PRIMARY KEY
- **display_name_en**: VARCHAR(100) NOT NULL
- **display_name_ar**: VARCHAR(100) NOT NULL
- **slug**: VARCHAR(100) UNIQUE NOT NULL
- **country_code**: VARCHAR(2) NOT NULL
- **region**: VARCHAR(100)
- **latitude**: DECIMAL(10, 8)
- **longitude**: DECIMAL(11, 8)
- **is_active**: BOOLEAN DEFAULT TRUE
- **created_at**: TIMESTAMP NOT NULL
- **updated_at**: TIMESTAMP NOT NULL

## Listing Tables

### Table: car_listings
- **id**: BIGINT PRIMARY KEY
- **title**: VARCHAR(100) NOT NULL
- **description**: TEXT NOT NULL
- **price**: DECIMAL(10, 2) NOT NULL
- **mileage**: INTEGER NOT NULL
- **model_year**: INTEGER NOT NULL
- **brand**: VARCHAR(50) NOT NULL
- **model**: VARCHAR(50) NOT NULL
- **vin**: VARCHAR(17)
- **stock_number**: VARCHAR(50)
- **exterior_color**: VARCHAR(50)
- **interior_color**: VARCHAR(50)
- **doors**: INTEGER
- **cylinders**: INTEGER
- **seller_id**: BIGINT NOT NULL (Foreign Key to users.id)
- **location_id**: BIGINT (Foreign Key to locations.id)
- **condition_id**: BIGINT (Foreign Key to car_conditions.id)
- **body_style_id**: BIGINT (Foreign Key to body_styles.id)
- **transmission_id**: BIGINT (Foreign Key to transmissions.id)
- **fuel_type_id**: BIGINT (Foreign Key to fuel_types.id)
- **drive_type_id**: BIGINT (Foreign Key to drive_types.id)
- **transmission**: VARCHAR(50)  // Note: This is a direct string field, separate from transmission_id FK
- **approved**: BOOLEAN DEFAULT FALSE
- **sold**: BOOLEAN DEFAULT FALSE
- **archived**: BOOLEAN DEFAULT FALSE
- **expired**: BOOLEAN DEFAULT FALSE
- **is_user_active**: BOOLEAN DEFAULT TRUE
- **expiration_date**: TIMESTAMP
- **created_at**: TIMESTAMP NOT NULL
- **updated_at**: TIMESTAMP NOT NULL

### Table: listing_media
- **id**: BIGINT PRIMARY KEY
- **listing_id**: BIGINT NOT NULL (Foreign Key to car_listings.id)
- **file_key**: VARCHAR(255) NOT NULL
- **file_name**: VARCHAR(255) NOT NULL
- **content_type**: VARCHAR(100) NOT NULL
- **size**: BIGINT NOT NULL
- **sort_order**: INTEGER DEFAULT 0
- **is_primary**: BOOLEAN DEFAULT FALSE
- **media_type**: VARCHAR(20) NOT NULL CHECK (media_type IN ('image', 'video'))
- **created_at**: TIMESTAMP NOT NULL

## User Feature Tables

### Table: favorites
- **id**: BIGINT PRIMARY KEY
- **user_id**: BIGINT NOT NULL (Foreign Key to users.id)
- **car_listing_id**: BIGINT NOT NULL (Foreign Key to car_listings.id)
- **created_at**: TIMESTAMP NOT NULL
- UNIQUE CONSTRAINT on (user_id, car_listing_id)

## Car Attributes Reference Tables

### Table: car_brands
- **id**: BIGINT PRIMARY KEY
- **name**: VARCHAR(50) NOT NULL
- **slug**: VARCHAR(100) UNIQUE NOT NULL
- **display_name_en**: VARCHAR(100) NOT NULL
- **display_name_ar**: VARCHAR(100) NOT NULL
- **is_active**: BOOLEAN DEFAULT TRUE

### Table: car_models
- **id**: BIGINT PRIMARY KEY
- **brand_id**: BIGINT NOT NULL (Foreign Key to car_brands.id)
- **name**: VARCHAR(50) NOT NULL
- **slug**: VARCHAR(100) UNIQUE NOT NULL
- **display_name_en**: VARCHAR(100) NOT NULL
- **display_name_ar**: VARCHAR(100) NOT NULL
- **is_active**: BOOLEAN DEFAULT TRUE

### Table: car_trims
- **id**: BIGINT PRIMARY KEY
- **model_id**: BIGINT NOT NULL (Foreign Key to car_models.id)
- **name**: VARCHAR(50) NOT NULL
- **display_name_en**: VARCHAR(100) NOT NULL
- **display_name_ar**: VARCHAR(100) NOT NULL
- **is_active**: BOOLEAN DEFAULT TRUE

### Table: car_conditions
- **id**: BIGINT PRIMARY KEY
- **name**: VARCHAR(20) NOT NULL
- **display_name_en**: VARCHAR(50) NOT NULL
- **display_name_ar**: VARCHAR(50) NOT NULL

### Table: drive_types
- **id**: BIGINT PRIMARY KEY
- **name**: VARCHAR(20) NOT NULL
- **display_name_en**: VARCHAR(50) NOT NULL
- **display_name_ar**: VARCHAR(50) NOT NULL

### Table: body_styles
- **id**: BIGINT PRIMARY KEY
- **name**: VARCHAR(20) NOT NULL
- **display_name_en**: VARCHAR(50) NOT NULL
- **display_name_ar**: VARCHAR(50) NOT NULL

### Table: fuel_types
- **id**: BIGINT PRIMARY KEY
- **name**: VARCHAR(20) NOT NULL
- **display_name_en**: VARCHAR(50) NOT NULL
- **display_name_ar**: VARCHAR(50) NOT NULL

### Table: transmissions
- **id**: BIGINT PRIMARY KEY
- **name**: VARCHAR(20) NOT NULL
- **display_name_en**: VARCHAR(50) NOT NULL
- **display_name_ar**: VARCHAR(50) NOT NULL

## Pricing & Ad Services Tables (Future Implementation)

### Table: ad_packages
- **id**: BIGINT PRIMARY KEY
- **name**: VARCHAR(50) NOT NULL
- **description**: TEXT
- **price**: DECIMAL(10, 2) NOT NULL
- **duration_days**: INTEGER NOT NULL
- **max_photos**: INTEGER NOT NULL
- **is_featured**: BOOLEAN DEFAULT FALSE
- **is_active**: BOOLEAN DEFAULT TRUE
- **created_at**: TIMESTAMP NOT NULL
- **updated_at**: TIMESTAMP NOT NULL

### Table: ad_services
- **id**: BIGINT PRIMARY KEY
- **name**: VARCHAR(50) NOT NULL
- **description**: TEXT
- **price**: DECIMAL(10, 2) NOT NULL
- **is_active**: BOOLEAN DEFAULT TRUE
- **created_at**: TIMESTAMP NOT NULL
- **updated_at**: TIMESTAMP NOT NULL

### Table: listing_packages
- **id**: BIGINT PRIMARY KEY
- **listing_id**: BIGINT NOT NULL (Foreign Key to car_listings.id)
- **package_id**: BIGINT NOT NULL (Foreign Key to ad_packages.id)
- **purchase_date**: TIMESTAMP NOT NULL
- **expiry_date**: TIMESTAMP NOT NULL
- **price_paid**: DECIMAL(10, 2) NOT NULL
- **created_at**: TIMESTAMP NOT NULL
- **updated_at**: TIMESTAMP NOT NULL

### Table: listing_services
- **id**: BIGINT PRIMARY KEY
- **listing_id**: BIGINT NOT NULL (Foreign Key to car_listings.id)
- **service_id**: BIGINT NOT NULL (Foreign Key to ad_services.id)
- **purchase_date**: TIMESTAMP NOT NULL
- **price_paid**: DECIMAL(10, 2) NOT NULL
- **created_at**: TIMESTAMP NOT NULL

## Relationships

- **users** has many **car_listings** (via `CarListing.seller`)
- **users** has many **roles** through **user_roles**
- **users** has many **favorites** (via `Favorite.user`)
- **car_listings** belongs to **users** (seller)
- **car_listings** belongs to **locations** (via `CarListing.location`)
- **car_listings** belongs to **car_conditions** (via `CarListing.condition`)
- **car_listings** belongs to **body_styles** (via `CarListing.bodyStyle`)
- **car_listings** belongs to **transmissions** (via `CarListing.transmissionType` for the entity, `transmission_id` for the FK)
- **car_listings** belongs to **fuel_types** (via `CarListing.fuelType`)
- **car_listings** belongs to **drive_types** (via `CarListing.driveType`)
- **car_listings** has many **listing_media** (via `ListingMedia.carListing` and `CarListing.media`)
- **car_listings** has many **favorites** (via `Favorite.carListing`)
- **car_brands** has many **car_models** (via `CarModel.brand` and `CarBrand.models`)
- **car_models** has many **car_trims** (via `CarTrim.model` and `CarModel.trims`)
- **car_models** belongs to **car_brands**
- **car_trims** belongs to **car_models**
- **car_listings** has many **ad_packages** through **listing_packages** (Future)
- **car_listings** has many **ad_services** through **listing_services** (Future)

## Entity Relationship Diagram (ERD) - Simplified based on existing entities

```
[users] 1------* [car_listings] *------1 [locations]
  |  \              |
  |   \             +--*--1 [car_conditions]
  |    \            +--*--1 [body_styles]
  |     \           +--*--1 [transmissions]
  |      \          +--*--1 [fuel_types]
  |       \         +--*--1 [drive_types]
  |        \        |
  |         \       *-- [listing_media]
  |          \      |
  |           \     |
[user_roles]    *--[favorites]
  |              
  |              
[roles]        

[car_brands] 1--* [car_models] 1--* [car_trims]

(Pricing & Ad Services tables like ad_packages, listing_packages etc. are future and not shown in this simplified ERD of current entities)
```

## Notes

1.  **Indexing Strategy**:
    - Indexes on foreign keys (e.g., `seller_id`, `location_id` in `car_listings`)
    - Indexes on commonly filtered fields (e.g., `brand`, `model`, `price` in `car_listings`)
    - Full-text search indexes on description fields

2.  **Data Migrations**:
    - Flyway is used to manage database schema versioning.
    - Migration scripts are stored in `src/main/resources/db/migration`.
    - Note: The initial migration script (`V1__Initial_Schema.sql`) may contain definitions for tables or columns (e.g., `makes`, `models` with different structures, additional columns in attribute tables like `slug`, `created_at`) that are not reflected in the current Java entities and therefore not in this schema document. This document aims to reflect the Java entity source of truth.

3.  **Data Integrity**:
    - Foreign key constraints ensure referential integrity.
    - Cascade delete for certain relationships (e.g., listing images when a listing is deleted) should be handled by JPA configurations or database constraints as appropriate.

## Database Triggers

### Timestamp Auto-Update Triggers
The `updated_at` timestamps in tables like `users`, `locations`, `car_listings` etc., are generally handled by JPA's `@PreUpdate` lifecycle callbacks in the entities or by database-level triggers if configured. For example, `CarListing.java` uses a PostgreSQL trigger, while `User.java` and `Location.java` use `@PreUpdate`.
This schema document does not list specific trigger names unless they are a critical part of the design not covered by entity behavior.
