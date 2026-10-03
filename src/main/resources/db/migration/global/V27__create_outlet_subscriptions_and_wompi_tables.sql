-- =============================================================================
-- V27: MODULO DE SUSCRIPCIONES POR TIENDA Y PASARELA WOMPI
-- 1. Insertar categorias y estados de suscripcion en master_data (STSSUB)
-- 2. Crear tabla public.outlet_subscription para control de pagos por tienda
-- 3. Crear tabla public.subscription_payment para transacciones y auditoria Wompi
-- 4. Extender tabla public.company con auditoria de aceptacion de politicas
-- 5. Inicializar suscripciones para tiendas existentes
-- =============================================================================

-- 1. Categoria STS_SUBSCRIPTIONS (STSSUB) bajo ROOTSTS
INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES (
    'STS_SUBSCRIPTIONS',
    'STSSUB',
    (SELECT id FROM master_data WHERE short_name = 'ROOTSTS'),
    (SELECT id FROM master_data WHERE short_name = 'ACT')
) ON CONFLICT (short_name) DO NOTHING;

-- Estados individuales de suscripcion bajo STSSUB
INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES (
    'PRUEBA_GRATUITA',
    'SUB_TRIAL',
    (SELECT id FROM master_data WHERE short_name = 'STSSUB'),
    (SELECT id FROM master_data WHERE short_name = 'ACT')
) ON CONFLICT (short_name) DO NOTHING;

INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES (
    'ACTIVA',
    'SUB_ACT',
    (SELECT id FROM master_data WHERE short_name = 'STSSUB'),
    (SELECT id FROM master_data WHERE short_name = 'ACT')
) ON CONFLICT (short_name) DO NOTHING;

INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES (
    'PERIODO_GRACIA',
    'SUB_GRACE',
    (SELECT id FROM master_data WHERE short_name = 'STSSUB'),
    (SELECT id FROM master_data WHERE short_name = 'ACT')
) ON CONFLICT (short_name) DO NOTHING;

INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES (
    'SUSPENDIDA',
    'SUB_SUSP',
    (SELECT id FROM master_data WHERE short_name = 'STSSUB'),
    (SELECT id FROM master_data WHERE short_name = 'ACT')
) ON CONFLICT (short_name) DO NOTHING;

INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES (
    'CANCELADA',
    'SUB_CANC',
    (SELECT id FROM master_data WHERE short_name = 'STSSUB'),
    (SELECT id FROM master_data WHERE short_name = 'ACT')
) ON CONFLICT (short_name) DO NOTHING;

-- 2. Tabla public.outlet_subscription
CREATE TABLE IF NOT EXISTS public.outlet_subscription (
    id BIGSERIAL PRIMARY KEY,
    outlet_id BIGINT NOT NULL UNIQUE REFERENCES public.outlet(id) ON DELETE CASCADE,
    company_id BIGINT NOT NULL REFERENCES public.company(id) ON DELETE CASCADE,
    status_id BIGINT NOT NULL REFERENCES public.master_data(id),
    billing_day INTEGER NOT NULL DEFAULT 28,
    amount_cop NUMERIC(12, 2) NOT NULL DEFAULT 60000.00,
    trial_ends_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    current_period_start TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    current_period_end TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    grace_period_end TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_outlet_sub_outlet ON public.outlet_subscription(outlet_id);
CREATE INDEX IF NOT EXISTS idx_outlet_sub_company ON public.outlet_subscription(company_id);
CREATE INDEX IF NOT EXISTS idx_outlet_sub_status ON public.outlet_subscription(status_id);
CREATE INDEX IF NOT EXISTS idx_outlet_sub_period_end ON public.outlet_subscription(current_period_end);

-- 3. Tabla public.subscription_payment
CREATE TABLE IF NOT EXISTS public.subscription_payment (
    id BIGSERIAL PRIMARY KEY,
    outlet_subscription_id BIGINT NOT NULL REFERENCES public.outlet_subscription(id) ON DELETE CASCADE,
    outlet_id BIGINT NOT NULL REFERENCES public.outlet(id) ON DELETE CASCADE,
    company_id BIGINT NOT NULL REFERENCES public.company(id) ON DELETE CASCADE,
    wompi_transaction_id VARCHAR(100) UNIQUE,
    wompi_reference VARCHAR(100) NOT NULL UNIQUE,
    payment_method_type VARCHAR(50),
    amount_in_cents BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'COP',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    checksum_sent VARCHAR(150),
    webhook_payload TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sub_pay_ref ON public.subscription_payment(wompi_reference);
CREATE INDEX IF NOT EXISTS idx_sub_pay_tx_id ON public.subscription_payment(wompi_transaction_id);
CREATE INDEX IF NOT EXISTS idx_sub_pay_outlet ON public.subscription_payment(outlet_id);
CREATE INDEX IF NOT EXISTS idx_sub_pay_status ON public.subscription_payment(status);

-- 4. Extender tabla company con campos de auditoria de politicas
ALTER TABLE public.company ADD COLUMN IF NOT EXISTS policies_accepted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE public.company ADD COLUMN IF NOT EXISTS policies_accepted_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE public.company ADD COLUMN IF NOT EXISTS policies_version VARCHAR(20) DEFAULT 'v1.0';

-- 5. Inicializar suscripciones para tiendas existentes que esten vinculadas a una empresa
INSERT INTO public.outlet_subscription (
    outlet_id,
    company_id,
    status_id,
    billing_day,
    amount_cop,
    trial_ends_at,
    current_period_start,
    current_period_end,
    grace_period_end,
    created_at,
    updated_at
)
SELECT
    o.id,
    o.company_id,
    (SELECT id FROM master_data WHERE short_name = 'SUB_ACT'),
    28,
    60000.00,
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    CURRENT_TIMESTAMP + INTERVAL '37 days',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM public.outlet o
WHERE o.company_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM public.outlet_subscription os WHERE os.outlet_id = o.id
  );
