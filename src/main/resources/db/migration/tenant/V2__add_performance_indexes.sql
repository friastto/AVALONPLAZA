-- Migracion Tenant V2: Indices de rendimiento para POS, Ventas, Cajas y Catalogo de Tienda
CREATE INDEX IF NOT EXISTS idx_cash_expenses_session ON cash_expenses (cash_session_id);
CREATE INDEX IF NOT EXISTS idx_cash_pickups_session ON cash_pickups (session_id);
CREATE INDEX IF NOT EXISTS idx_orders_outlet ON orders (outlet_id);
CREATE INDEX IF NOT EXISTS idx_product_outlet_lookup ON product_outlet (outlet_id, status_id, local_name);

CREATE INDEX IF NOT EXISTS idx_product_outlet_name_trgm ON product_outlet USING gin (local_name public.gin_trgm_ops);
