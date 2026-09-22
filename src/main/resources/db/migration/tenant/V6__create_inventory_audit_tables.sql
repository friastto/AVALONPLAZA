-- Flyway V6 Tenant: Add inventory_audit_session, inventory_audit_item and inventory_audit_signature tables
CREATE TABLE IF NOT EXISTS inventory_audit_session (
    id                      BIGSERIAL       PRIMARY KEY,
    outlet_id               BIGINT          NOT NULL,
    company_id              BIGINT,
    status                  VARCHAR(30)     NOT NULL DEFAULT 'IN_PROGRESS',
    opened_by_user_id       BIGINT          NOT NULL,
    opened_by_name          VARCHAR(150),
    closed_by_user_id       BIGINT,
    closed_by_name          VARCHAR(150),
    notes                   VARCHAR(500),
    total_software_value    NUMERIC(15,2)   DEFAULT 0.00,
    total_physical_value    NUMERIC(15,2)   DEFAULT 0.00,
    total_difference_value  NUMERIC(15,2)   DEFAULT 0.00,
    opened_at               TIMESTAMP       NOT NULL DEFAULT NOW(),
    closed_at               TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory_audit_item (
    id                  BIGSERIAL       PRIMARY KEY,
    audit_session_id    BIGINT          NOT NULL REFERENCES inventory_audit_session(id) ON DELETE CASCADE,
    product_outlet_id   BIGINT          NOT NULL,
    product_name        VARCHAR(255)    NOT NULL,
    barcode             VARCHAR(100),
    unit_measure        VARCHAR(20)     NOT NULL DEFAULT 'UND',
    unit_price          NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    software_stock      NUMERIC(12,3)   NOT NULL DEFAULT 0.00,
    software_value      NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    physical_stock      NUMERIC(12,3),
    physical_value      NUMERIC(15,2),
    difference_stock    NUMERIC(12,3),
    difference_value    NUMERIC(15,2),
    discrepancy_reason  VARCHAR(255),
    counted_by_user_id  BIGINT,
    counted_by_name     VARCHAR(150),
    status              VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS inventory_audit_signature (
    id                  BIGSERIAL       PRIMARY KEY,
    audit_session_id    BIGINT          NOT NULL REFERENCES inventory_audit_session(id) ON DELETE CASCADE,
    signer_type         VARCHAR(30)     NOT NULL,
    signer_user_id      BIGINT          NOT NULL,
    signer_name         VARCHAR(150)    NOT NULL,
    signature_base64    TEXT            NOT NULL,
    signed_at           TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_item_session_id ON inventory_audit_item(audit_session_id);
CREATE INDEX IF NOT EXISTS idx_audit_signature_session_id ON inventory_audit_signature(audit_session_id);
CREATE INDEX IF NOT EXISTS idx_audit_session_outlet ON inventory_audit_session(outlet_id, status);
