-- V8: Add ADMOULT role to master_data and default_cash_threshold_amount to company table

-- 1. Insert ADMOULT role in master_data under GERENTE
INSERT INTO master_data (full_name, short_name, parent_id, status_id)
SELECT 'ADMINISTRADOR_OUTLET', 'ADMOULT', g.id, a.id
FROM master_data g, master_data a
WHERE g.short_name = 'GERENTE' AND a.short_name = 'ACT'
ON CONFLICT DO NOTHING;

-- 2. Add default_cash_threshold_amount to company table
ALTER TABLE company ADD COLUMN IF NOT EXISTS default_cash_threshold_amount NUMERIC(19, 2);
