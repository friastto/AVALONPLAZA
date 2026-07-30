-- Migracion Tenant V3: Anadir columna version para control de concurrencia optimista @Version
ALTER TABLE product_outlet ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE credit_account ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
