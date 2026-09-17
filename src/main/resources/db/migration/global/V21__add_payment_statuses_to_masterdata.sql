-- =============================================================================
-- V21: AGREGAR ESTADOS DE PAGO (STSPAY, PAY_PEN, PAY_PAD, PAY_REF) A MASTER_DATA
--      Y SANEAR PEDIDOS HISTORICOS QUE TENIAN payment_status_id = 2 (ROOTSTS)
-- =============================================================================

-- 1. Insertar la categoria STS_PAYMENTS (STSPAY) bajo ROOTSTS
INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES (
    'STS_PAYMENTS',
    'STSPAY',
    (SELECT id FROM master_data WHERE short_name = 'ROOTSTS'),
    (SELECT id FROM master_data WHERE short_name = 'ACT')
) ON CONFLICT (short_name) DO NOTHING;

-- 2. Insertar frutos de estado de pago bajo STSPAY
INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES (
    'PAGO_PENDIENTE',
    'PAY_PEN',
    (SELECT id FROM master_data WHERE short_name = 'STSPAY'),
    (SELECT id FROM master_data WHERE short_name = 'ACT')
) ON CONFLICT (short_name) DO NOTHING;

INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES (
    'PAGO_PAGADO',
    'PAY_PAD',
    (SELECT id FROM master_data WHERE short_name = 'STSPAY'),
    (SELECT id FROM master_data WHERE short_name = 'ACT')
) ON CONFLICT (short_name) DO NOTHING;

INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES (
    'PAGO_REEMBOLSADO',
    'PAY_REF',
    (SELECT id FROM master_data WHERE short_name = 'STSPAY'),
    (SELECT id FROM master_data WHERE short_name = 'ACT')
) ON CONFLICT (short_name) DO NOTHING;

-- 3. Sanear pedidos entregados: payment_status_id pasa a PAY_PAD
UPDATE omnichannel_orders
SET payment_status_id = (SELECT id FROM master_data WHERE short_name = 'PAY_PAD')
WHERE order_status_id = (SELECT id FROM master_data WHERE short_name = 'ENT');

-- 4. Sanear pedidos en preparacion/pendientes: payment_status_id pasa a PAY_PEN si tenian 2
UPDATE omnichannel_orders
SET payment_status_id = (SELECT id FROM master_data WHERE short_name = 'PAY_PEN')
WHERE payment_status_id = 2;

-- 5. Sanear tabla orders clasica
UPDATE orders
SET payment_status_id = (SELECT id FROM master_data WHERE short_name = 'PAY_PAD')
WHERE payment_status_id = 2;