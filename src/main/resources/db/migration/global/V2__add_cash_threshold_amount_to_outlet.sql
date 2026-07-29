-- =============================================================================
-- V2: Add cash_threshold_amount to outlet in public schema
-- =============================================================================

ALTER TABLE outlet ADD COLUMN IF NOT EXISTS cash_threshold_amount NUMERIC(38, 2);
