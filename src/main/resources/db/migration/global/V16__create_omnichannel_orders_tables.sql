-- V16__create_omnichannel_orders_tables.sql
-- Migracion e independizacion de tablas para modulo Pedidos Omnicanal (omnichannel_orders)

CREATE TABLE IF NOT EXISTS omnichannel_orders (
    id BIGSERIAL PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT,
    outlet_id BIGINT NOT NULL,
    order_status_id BIGINT NOT NULL,
    payment_status_id BIGINT NOT NULL,
    payment_method_id BIGINT,
    subtotal NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    tax NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    total NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    claimed_by_user_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS omnichannel_order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES omnichannel_orders(id) ON DELETE CASCADE,
    product_outlet_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL,
    subtotal NUMERIC(15, 2) NOT NULL,
    dispatch_status_id BIGINT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS omnichannel_order_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES omnichannel_orders(id) ON DELETE CASCADE,
    previous_status_id BIGINT,
    new_status_id BIGINT NOT NULL,
    changed_by_user_id BIGINT,
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_omnichannel_orders_outlet_status ON omnichannel_orders(outlet_id, order_status_id);
CREATE INDEX IF NOT EXISTS idx_omnichannel_orders_customer ON omnichannel_orders(customer_id);
