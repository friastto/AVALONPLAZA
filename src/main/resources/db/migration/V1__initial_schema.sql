-- =============================================================================
-- Flyway Migration V1: Initial Schema Baseline (ApiAvalon)
-- PostgreSQL + PostGIS Extension
-- =============================================================================

-- Habilitar extensión espacial para ubicaciones (Outlet)
CREATE EXTENSION IF NOT EXISTS postgis;

-- =============================================================================
-- 1. MASTERDATA & PERSON
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
-- 2. OUTLET
-- =============================================================================

CREATE TABLE IF NOT EXISTS outlet (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    nit VARCHAR(255),
    name VARCHAR(255),
    address VARCHAR(255),
    phone VARCHAR(255),
    status_id BIGINT,
    location Geometry(Point, 4326),
    cash_threshold_amount NUMERIC(38, 2),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- =============================================================================
-- 3. USER & AUTHENTICATION
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
-- 4. CASHREGISTER
-- =============================================================================

CREATE TABLE IF NOT EXISTS cash_sessions (
    id BIGSERIAL PRIMARY KEY,
    outlet_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    opened_at TIMESTAMP NOT NULL,
    closed_at TIMESTAMP,
    initial_base NUMERIC(12, 2) NOT NULL,
    expected_cash NUMERIC(12, 2),
    actual_cash NUMERIC(12, 2),
    difference NUMERIC(12, 2),
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS cash_expenses (
    id BIGSERIAL PRIMARY KEY,
    cash_session_id BIGINT NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    registered_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS cash_pickups (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    reason VARCHAR(255),
    pickup_time TIMESTAMP NOT NULL
);

-- =============================================================================
-- 5. CREDIT
-- =============================================================================

CREATE TABLE IF NOT EXISTS credit_account (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    outlet_id BIGINT NOT NULL,
    credit_limit NUMERIC(15, 2) NOT NULL,
    current_debt NUMERIC(15, 2) NOT NULL,
    status_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_credit_account_client_outlet UNIQUE (client_id, outlet_id)
);

CREATE INDEX IF NOT EXISTS idx_credit_account_outlet ON credit_account (outlet_id);
CREATE INDEX IF NOT EXISTS idx_credit_account_client_outlet ON credit_account (client_id, outlet_id);

CREATE TABLE IF NOT EXISTS credit_transaction (
    id BIGSERIAL PRIMARY KEY,
    credit_account_id BIGINT NOT NULL,
    sale_id BIGINT,
    type VARCHAR(255) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    previous_debt NUMERIC(15, 2) NOT NULL,
    new_debt NUMERIC(15, 2) NOT NULL,
    notes VARCHAR(255),
    registered_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_credit_txn_account ON credit_transaction (credit_account_id);
CREATE INDEX IF NOT EXISTS idx_credit_txn_created_at ON credit_transaction (created_at);

-- =============================================================================
-- 6. PRODUCTS & INVENTORY
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

CREATE TABLE IF NOT EXISTS product_outlet (
    id BIGSERIAL PRIMARY KEY,
    local_name VARCHAR(255),
    local_description VARCHAR(255),
    stock INTEGER NOT NULL,
    unit_measure_id BIGINT NOT NULL,
    local_image_url text[],
    local_price NUMERIC(38, 2) NOT NULL,
    outlet_id BIGINT,
    status_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS barcode (
    id BIGSERIAL PRIMARY KEY,
    barcode VARCHAR(255) NOT NULL,
    product_outlet BIGINT NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_barcode_outlet UNIQUE (barcode, product_outlet)
);

-- =============================================================================
-- 7. SALES, ORDERS & RETURNS
-- =============================================================================

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_code UUID NOT NULL UNIQUE,
    total_amount NUMERIC(12, 2) NOT NULL,
    payment_method_id BIGINT NOT NULL,
    status_id BIGINT NOT NULL,
    outlet_id BIGINT NOT NULL,
    order_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity_in_base_units INTEGER NOT NULL,
    display_quantity VARCHAR(255) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL,
    unit_measure_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sales (
    id BIGSERIAL PRIMARY KEY,
    sale_code UUID NOT NULL UNIQUE,
    total_amount NUMERIC(12, 2) NOT NULL,
    amount_received NUMERIC(12, 2) NOT NULL,
    change_given NUMERIC(12, 2) NOT NULL,
    payment_method_id BIGINT NOT NULL,
    status_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    outlet_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    sale_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sale_items (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity_in_base_units INTEGER NOT NULL,
    display_quantity VARCHAR(255) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL,
    unit_measure_id BIGINT NOT NULL,
    sale_id BIGINT NOT NULL,
    CONSTRAINT fk_sale_items_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_returns (
    id BIGSERIAL PRIMARY KEY,
    return_code UUID NOT NULL UNIQUE,
    original_sale_id BIGINT NOT NULL,
    total_refund_amount NUMERIC(12, 2) NOT NULL,
    reason VARCHAR(20) NOT NULL,
    notes VARCHAR(500),
    resolution_type VARCHAR(20) NOT NULL,
    status_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    outlet_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    return_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS return_items (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity_in_base_units INTEGER NOT NULL,
    display_quantity VARCHAR(255) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL,
    return_id BIGINT NOT NULL,
    CONSTRAINT fk_return_items_return FOREIGN KEY (return_id) REFERENCES product_returns(id) ON DELETE CASCADE
);
