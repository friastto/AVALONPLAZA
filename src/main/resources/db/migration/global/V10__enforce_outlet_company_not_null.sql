-- =============================================================================
-- V10: Enforce mandatory Company association for all Outlets (NOT NULL Invariant)
-- =============================================================================

-- 1. Enforce NOT NULL and Foreign Key constraints on outlet.company_id
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
