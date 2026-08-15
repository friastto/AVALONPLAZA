-- Migracion Global V14: Anadir columna y relacion product_company_id en product_outlet para esquemas de empresa
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
