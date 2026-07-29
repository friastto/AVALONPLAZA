-- Enable Spatial Extension if needed for location calculations
CREATE EXTENSION IF NOT EXISTS postgis;

-- =============================================================================
-- 1. MASTERDATA & PERSON (SHARED GLOBAL SCHEMA)
-- =============================================================================

CREATE TABLE IF NOT EXISTS master_data (
    id BIGSERIAL PRIMARY KEY,
    short_name VARCHAR(255),
    full_name VARCHAR(255),
    parent_id BIGINT,
    status_id BIGINT,
    CONSTRAINT uk_master_data_short_name UNIQUE (short_name),
    CONSTRAINT uk_master_data_full_name UNIQUE (full_name)
);

CREATE TABLE IF NOT EXISTS person (
    id BIGSERIAL PRIMARY KEY,
    number_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    identification_id BIGINT NOT NULL,
    sex_id BIGINT,
    phone_number BIGINT,
    email VARCHAR(255),
    status_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_person_identification_number UNIQUE (identification_id, number_id)
);

-- =============================================================================
-- 2. USER & AUTHENTICATION (SHARED GLOBAL SCHEMA)
-- =============================================================================

CREATE TABLE IF NOT EXISTS user_avalon (
    id BIGSERIAL PRIMARY KEY,
    person_id BIGINT,
    user_name VARCHAR(255) NOT NULL UNIQUE,
    hash_salt VARCHAR(255) NOT NULL,
    hash_password VARCHAR(255) NOT NULL,
    status_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS role_assignment (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    role_id BIGINT,
    outlet_id BIGINT,
    status BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    pin VARCHAR(255) NOT NULL,
    verification_token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS refresh_token (
    id UUID PRIMARY KEY,
    refresh_token VARCHAR(255) NOT NULL UNIQUE,
    user_avalon_id BIGINT NOT NULL,
    expiry_date TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL
);

-- =============================================================================
-- 3. GLOBAL PRODUCT CATALOG
-- =============================================================================

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    category_id BIGINT,
    unit_measure_id BIGINT,
    image_url VARCHAR(255),
    status_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_products_category ON products (category_id);
CREATE INDEX IF NOT EXISTS idx_products_status ON products (status_id);
