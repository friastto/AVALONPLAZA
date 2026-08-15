-- Migracion Global V14: Crear la tabla base public.product_outlet y anadir product_company_id en esquemas company_*
CREATE TABLE IF NOT EXISTS public.product_outlet (
    id BIGSERIAL PRIMARY KEY,
    product_company_id BIGINT,
    local_name VARCHAR(255),
    local_description TEXT,
    stock INT NOT NULL DEFAULT 0,
    unit_measure_id BIGINT NOT NULL DEFAULT 1,
    local_image_url TEXT[],
    local_price NUMERIC(38,2) NOT NULL DEFAULT 0.00,
    outlet_id BIGINT,
    status_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

DO $$
DECLARE
    rec RECORD;
    company_schema_name TEXT;
BEGIN
    FOR rec IN SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'company_%' LOOP
        company_schema_name := rec.schema_name;

        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = company_schema_name AND table_name = 'product_outlet') THEN
            EXECUTE format('ALTER TABLE %I.product_outlet ADD COLUMN IF NOT EXISTS product_company_id BIGINT', company_schema_name);
        END IF;
    END LOOP;
END $$;
