-- V15__create_orders_and_claims_tables.sql
-- Tablas para modulo de Pedidos Omnicanal (Orders) y Reclamos (Claims)

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

CREATE TABLE IF NOT EXISTS order_claims (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES omnichannel_orders(id) ON DELETE CASCADE,
    customer_id BIGINT,
    claim_type_id BIGINT NOT NULL,
    status_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    resolution_notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_claim_items (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES order_claims(id) ON DELETE CASCADE,
    order_item_id BIGINT NOT NULL REFERENCES omnichannel_order_items(id) ON DELETE CASCADE,
    quantity_affected INT NOT NULL,
    reason VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS order_claim_photos (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES order_claims(id) ON DELETE CASCADE,
    photo_url TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_omnichannel_orders_outlet_status ON omnichannel_orders(outlet_id, order_status_id);
CREATE INDEX IF NOT EXISTS idx_omnichannel_orders_customer ON omnichannel_orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_order_claims_order ON order_claims(order_id);
