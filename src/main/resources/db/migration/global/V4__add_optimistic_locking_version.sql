-- Migracion Global V4: Anadir columna version en tabla product_outlet para SuperAdmin / Catalogo Global
ALTER TABLE product_outlet ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
