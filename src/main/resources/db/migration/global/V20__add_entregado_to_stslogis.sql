-- =============================================================================
-- V20: AGREGAR ESTADO ENTREGADO (ENT) A LA RAMA STS_LOGISTICS (STSLOGIS)
--      Y SANEAR PEDIDOS HISTORICOS QUE TENIAN order_status_id = 4 (INA)
-- =============================================================================

-- 1. Insertar el nodo ENTREGADO (ENT) como hijo de STS_LOGISTICS (STSLOGIS)
INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES (
    'ENTREGADO',
    'ENT',
    (SELECT id FROM master_data WHERE short_name = 'STSLOGIS'),
    (SELECT id FROM master_data WHERE short_name = 'ACT')
) ON CONFLICT (short_name) DO NOTHING;

-- 2. Sanear pedidos existentes en omnichannel_orders que se habian guardado con order_status_id = 4
UPDATE omnichannel_orders
SET order_status_id = (SELECT id FROM master_data WHERE short_name = 'ENT')
WHERE order_status_id = 4;

-- 3. Sanear pedidos en tabla orders clasica si los hubiera con order_status_id = 4
UPDATE orders
SET order_status_id = (SELECT id FROM master_data WHERE short_name = 'ENT')
WHERE order_status_id = 4;

-- 4. Sanear historial de estados en omnichannel_order_status_history
UPDATE omnichannel_order_status_history
SET new_status_id = (SELECT id FROM master_data WHERE short_name = 'ENT')
WHERE new_status_id = 4;

-- 5. Sanear historial de estados en order_status_history clasica
UPDATE order_status_history
SET new_status_id = (SELECT id FROM master_data WHERE short_name = 'ENT')
WHERE new_status_id = 4;