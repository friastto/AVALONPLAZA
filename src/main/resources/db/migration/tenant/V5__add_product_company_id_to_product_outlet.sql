-- Migration Tenant V5: Anadir columna product_company_id a la tabla product_outlet en esquemas tenant
ALTER TABLE product_outlet ADD COLUMN IF NOT EXISTS product_company_id BIGINT;
