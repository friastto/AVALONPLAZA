-- =============================================================================
-- CASH REGISTER
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

CREATE INDEX IF NOT EXISTS idx_cash_sessions_employee_status ON cash_sessions (employee_id, status);
CREATE INDEX IF NOT EXISTS idx_cash_sessions_opened_at ON cash_sessions (opened_at DESC);

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
-- CREDIT
-- =============================================================================

CREATE TABLE IF NOT EXISTS credit_account (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL UNIQUE,
    outlet_id BIGINT NOT NULL,
    credit_limit NUMERIC(15, 2) NOT NULL,
    current_debt NUMERIC(15, 2) NOT NULL,
    status_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_credit_account_status ON credit_account (status_id);

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

CREATE INDEX IF NOT EXISTS idx_credit_txn_account_date ON credit_transaction (credit_account_id, created_at DESC);

-- =============================================================================
-- LOCAL INVENTORY & BARCODE
-- =============================================================================

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

CREATE INDEX IF NOT EXISTS idx_product_outlet_status ON product_outlet (status_id);

CREATE TABLE IF NOT EXISTS barcode (
    id BIGSERIAL PRIMARY KEY,
    barcode VARCHAR(255) NOT NULL UNIQUE,
    product_outlet BIGINT NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- =============================================================================
-- SALES, ORDERS & RETURNS
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

CREATE INDEX IF NOT EXISTS idx_orders_status_date ON orders (status_id, order_date DESC);

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

CREATE INDEX IF NOT EXISTS idx_sales_date ON sales (sale_date DESC);
CREATE INDEX IF NOT EXISTS idx_sales_client ON sales (client_id);
CREATE INDEX IF NOT EXISTS idx_sales_status ON sales (status_id);

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

CREATE INDEX IF NOT EXISTS idx_sale_items_sale ON sale_items (sale_id);
CREATE INDEX IF NOT EXISTS idx_sale_items_product ON sale_items (product_id);

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

-- =============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES BY OUTLET
-- =============================================================================

ALTER TABLE cash_sessions ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS outlet_isolation_policy ON cash_sessions;
CREATE POLICY outlet_isolation_policy ON cash_sessions USING (outlet_id = current_setting('app.current_outlet_id', true)::bigint);

ALTER TABLE credit_account ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS outlet_isolation_policy ON credit_account;
CREATE POLICY outlet_isolation_policy ON credit_account USING (outlet_id = current_setting('app.current_outlet_id', true)::bigint);

ALTER TABLE product_outlet ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS outlet_isolation_policy ON product_outlet;
CREATE POLICY outlet_isolation_policy ON product_outlet USING (outlet_id = current_setting('app.current_outlet_id', true)::bigint);

ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS outlet_isolation_policy ON orders;
CREATE POLICY outlet_isolation_policy ON orders USING (outlet_id = current_setting('app.current_outlet_id', true)::bigint);

ALTER TABLE sales ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS outlet_isolation_policy ON sales;
CREATE POLICY outlet_isolation_policy ON sales USING (outlet_id = current_setting('app.current_outlet_id', true)::bigint);

ALTER TABLE product_returns ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS outlet_isolation_policy ON product_returns;
CREATE POLICY outlet_isolation_policy ON product_returns USING (outlet_id = current_setting('app.current_outlet_id', true)::bigint);

