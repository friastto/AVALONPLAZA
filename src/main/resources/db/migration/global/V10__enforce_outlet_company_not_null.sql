-- =============================================================================
-- V10: Enforce mandatory Company association for all Outlets (NOT NULL Invariant)
-- =============================================================================

-- 1. Ensure at least one Default Company exists if table is empty
INSERT INTO public.company (nit, name, email, created_at)
SELECT '900000000-1', 'Empresa Matriz Avalon', 'contacto@avalon.org', NOW()
WHERE NOT EXISTS (SELECT 1 FROM public.company);

-- 2. Link any orphan outlets (company_id IS NULL) to the primary default company
UPDATE public.outlet
SET company_id = (SELECT id FROM public.company ORDER BY id ASC LIMIT 1)
WHERE company_id IS NULL;

-- 3. Enforce NOT NULL and Foreign Key constraints on outlet.company_id
ALTER TABLE public.outlet ALTER COLUMN company_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_outlet_company'
    ) THEN
        ALTER TABLE public.outlet 
        ADD CONSTRAINT fk_outlet_company 
        FOREIGN KEY (company_id) REFERENCES public.company(id);
    END IF;
END $$;
