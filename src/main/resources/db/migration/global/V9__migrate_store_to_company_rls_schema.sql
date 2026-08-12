-- =============================================================================
-- V9: Migration script to consolidate store_% schemas into company_% schemas with RLS
-- =============================================================================

DO $$
DECLARE
    rec RECORD;
    target_company_id BIGINT;
    company_schema_name TEXT;
BEGIN
    -- Loop through all existing store_% schemas
    FOR rec IN 
        SELECT schema_name 
        FROM information_schema.schemata 
        WHERE schema_name LIKE 'store_%'
    LOOP
        -- Extract store_id from schema_name (e.g. store_1 -> 1)
        BEGIN
            EXECUTE format('SELECT company_id FROM public.outlet WHERE id = %s', substring(rec.schema_name from 7))
            INTO target_company_id;
        EXCEPTION WHEN OTHERS THEN
            target_company_id := NULL;
        END;

        IF target_company_id IS NOT NULL THEN
            company_schema_name := 'company_' || target_company_id;

            -- Create company schema if it does not exist
            EXECUTE format('CREATE SCHEMA IF NOT EXISTS %I', company_schema_name);

            -- Migrate tables if they exist in store_X and not in company_Y
            -- product_outlet
            IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = rec.schema_name AND table_name = 'product_outlet') THEN
                EXECUTE format('CREATE TABLE IF NOT EXISTS %I.product_outlet (LIKE %I.product_outlet INCLUDING ALL)', company_schema_name, rec.schema_name);
                EXECUTE format('INSERT INTO %I.product_outlet SELECT * FROM %I.product_outlet ON CONFLICT DO NOTHING', company_schema_name, rec.schema_name);
                EXECUTE format('ALTER TABLE %I.product_outlet ENABLE ROW LEVEL SECURITY', company_schema_name);
                EXECUTE format('DROP POLICY IF EXISTS outlet_isolation_policy ON %I.product_outlet', company_schema_name);
                EXECUTE format('CREATE POLICY outlet_isolation_policy ON %I.product_outlet USING (outlet_id = current_setting(''app.current_outlet_id'', true)::bigint)', company_schema_name);
            END IF;

            -- sales
            IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = rec.schema_name AND table_name = 'sales') THEN
                EXECUTE format('CREATE TABLE IF NOT EXISTS %I.sales (LIKE %I.sales INCLUDING ALL)', company_schema_name, rec.schema_name);
                EXECUTE format('INSERT INTO %I.sales SELECT * FROM %I.sales ON CONFLICT DO NOTHING', company_schema_name, rec.schema_name);
                EXECUTE format('ALTER TABLE %I.sales ENABLE ROW LEVEL SECURITY', company_schema_name);
                EXECUTE format('DROP POLICY IF EXISTS outlet_isolation_policy ON %I.sales', company_schema_name);
                EXECUTE format('CREATE POLICY outlet_isolation_policy ON %I.sales USING (outlet_id = current_setting(''app.current_outlet_id'', true)::bigint)', company_schema_name);
            END IF;

            -- cash_sessions
            IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = rec.schema_name AND table_name = 'cash_sessions') THEN
                EXECUTE format('CREATE TABLE IF NOT EXISTS %I.cash_sessions (LIKE %I.cash_sessions INCLUDING ALL)', company_schema_name, rec.schema_name);
                EXECUTE format('INSERT INTO %I.cash_sessions SELECT * FROM %I.cash_sessions ON CONFLICT DO NOTHING', company_schema_name, rec.schema_name);
                EXECUTE format('ALTER TABLE %I.cash_sessions ENABLE ROW LEVEL SECURITY', company_schema_name);
                EXECUTE format('DROP POLICY IF EXISTS outlet_isolation_policy ON %I.cash_sessions', company_schema_name);
                EXECUTE format('CREATE POLICY outlet_isolation_policy ON %I.cash_sessions USING (outlet_id = current_setting(''app.current_outlet_id'', true)::bigint)', company_schema_name);
            END IF;

            -- orders
            IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = rec.schema_name AND table_name = 'orders') THEN
                EXECUTE format('CREATE TABLE IF NOT EXISTS %I.orders (LIKE %I.orders INCLUDING ALL)', company_schema_name, rec.schema_name);
                EXECUTE format('INSERT INTO %I.orders SELECT * FROM %I.orders ON CONFLICT DO NOTHING', company_schema_name, rec.schema_name);
                EXECUTE format('ALTER TABLE %I.orders ENABLE ROW LEVEL SECURITY', company_schema_name);
                EXECUTE format('DROP POLICY IF EXISTS outlet_isolation_policy ON %I.orders', company_schema_name);
                EXECUTE format('CREATE POLICY outlet_isolation_policy ON %I.orders USING (outlet_id = current_setting(''app.current_outlet_id'', true)::bigint)', company_schema_name);
            END IF;

        END IF;
    END LOOP;
END $$;
