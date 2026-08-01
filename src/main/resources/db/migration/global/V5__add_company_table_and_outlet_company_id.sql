-- Create company table in public schema
CREATE TABLE IF NOT EXISTS company (
    id BIGSERIAL PRIMARY KEY,
    nit VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    status_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Add company_id column to outlet table
ALTER TABLE outlet ADD COLUMN IF NOT EXISTS company_id BIGINT;
