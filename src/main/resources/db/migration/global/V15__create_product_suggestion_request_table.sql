-- Flyway V15 Global: Create product_suggestion_request table for Store Outlet -> Company approvals
CREATE TABLE IF NOT EXISTS public.product_suggestion_request (
    id                  BIGSERIAL       PRIMARY KEY,
    outlet_id           BIGINT          NOT NULL,
    company_id          BIGINT          NOT NULL,
    requested_by        BIGINT          NOT NULL,
    name                VARCHAR(255)    NOT NULL,
    description         TEXT,
    barcode             VARCHAR(100),
    price               NUMERIC(12, 2)  NOT NULL DEFAULT 0,
    unit_measure_id     BIGINT          NOT NULL DEFAULT 1,
    image_urls          TEXT[],
    status              VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    rejection_reason    TEXT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Index for fast querying pending suggestions by company
CREATE INDEX IF NOT EXISTS idx_product_suggestion_company_status
    ON public.product_suggestion_request (company_id, status);

-- Index for fast querying by store outlet
CREATE INDEX IF NOT EXISTS idx_product_suggestion_outlet_id
    ON public.product_suggestion_request (outlet_id);
