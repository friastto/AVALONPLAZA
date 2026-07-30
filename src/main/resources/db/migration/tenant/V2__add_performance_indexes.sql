-- Migracion Tenant V2: Indices de rendimiento para POS, Ventas, Cajas y Catalogo de Tienda
CREATE INDEX IF NOT EXISTS idx_cash_expenses_session ON cash_expenses (cash_session_id);
CREATE INDEX IF NOT EXISTS idx_cash_pickups_session ON cash_pickups (session_id);
CREATE INDEX IF NOT EXISTS idx_orders_outlet ON orders (outlet_id);
CREATE INDEX IF NOT EXISTS idx_product_outlet_lookup ON product_outlet (outlet_id, status_id, local_name);

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_product_outlet_name_trgm ON product_outlet USING gin (local_name gin_trgm_ops);

-- Anadir columna version para control de concurrencia optimista @Version
ALTER TABLE product_outlet ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE credit_account ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
