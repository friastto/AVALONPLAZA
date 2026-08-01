-- Flyway V4 Tenant: Add stock_movement (Kardex) table to each store schema
CREATE TABLE IF NOT EXISTS stock_movement (
    id              BIGSERIAL       PRIMARY KEY,
    product_outlet_id BIGINT        NOT NULL,
    outlet_id       BIGINT          NOT NULL,
    movement_type   VARCHAR(30)     NOT NULL,   -- INGESTION, SALE, MERMA, ADJUSTMENT_SURPLUS, TRANSFER
    quantity_before INTEGER         NOT NULL,
    quantity_after  INTEGER         NOT NULL,
    quantity_delta  INTEGER         NOT NULL,   -- positive = entrada, negative = salida
    reason          VARCHAR(500),
    operator_id     BIGINT,                     -- user_avalon.id who made the change
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Index for fast lookup by product_outlet_id (Kardex query)
CREATE INDEX IF NOT EXISTS idx_stock_movement_product_outlet_id
    ON stock_movement (product_outlet_id);

-- Index for temporal audit queries
CREATE INDEX IF NOT EXISTS idx_stock_movement_created_at
    ON stock_movement (created_at DESC);
