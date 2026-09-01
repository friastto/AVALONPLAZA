-- Migration V19: Add company_id column to role_assignment for GERGEN company-level scoping
ALTER TABLE public.role_assignment
    ADD COLUMN IF NOT EXISTS company_id BIGINT;

ALTER TABLE public.role_assignment
    ADD CONSTRAINT fk_role_assignment_company
        FOREIGN KEY (company_id) REFERENCES public.company(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_role_assignment_company ON public.role_assignment (company_id);
