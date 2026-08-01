-- Flyway V6 Global: Create product_company table (Level 2 product hierarchy)
CREATE TABLE IF NOT EXISTS public.product_company (
    id              BIGSERIAL       PRIMARY KEY,
    product_id      BIGINT          NOT NULL,
    company_id      BIGINT          NOT NULL,
    custom_price    NUMERIC(12, 2)  NOT NULL DEFAULT 0,
    custom_image_url VARCHAR(1000),
    status_id       BIGINT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    version         BIGINT          NOT NULL DEFAULT 0,
    UNIQUE (product_id, company_id)
);

-- Index for fast lookups by company
CREATE INDEX IF NOT EXISTS idx_product_company_company_id
    ON public.product_company (company_id);

-- Index for fast lookups by product
CREATE INDEX IF NOT EXISTS idx_product_company_product_id
    ON public.product_company (product_id);
