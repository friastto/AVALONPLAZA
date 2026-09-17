-- =============================================================================
-- V22: JERARQUIZACION DE METODOS DE PAGO (MPG) EN SUBRAMAS CONTABLES
--      (MPG_CASH, MPG_CARD, MPG_DIGITAL, MPG_CREDIT)
-- =============================================================================

-- 1. Insertar subcategorias contables bajo MPG (TYPE_METODO_DE_PAGO)
INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES (
    'PAGOS_EN_EFECTIVO',
    'MPG_CASH',
    (SELECT id FROM master_data WHERE short_name = 'MPG'),
    (SELECT id FROM master_data WHERE short_name = 'ACT')
) ON CONFLICT (short_name) DO NOTHING;

INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES (
    'PAGOS_CON_TARJETA',
    'MPG_CARD',
    (SELECT id FROM master_data WHERE short_name = 'MPG'),
    (SELECT id FROM master_data WHERE short_name = 'ACT')
) ON CONFLICT (short_name) DO NOTHING;

INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES (
    'PAGOS_DIGITALES',
    'MPG_DIGITAL',
    (SELECT id FROM master_data WHERE short_name = 'MPG'),
    (SELECT id FROM master_data WHERE short_name = 'ACT')
) ON CONFLICT (short_name) DO NOTHING;

INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES (
    'PAGOS_A_CREDITO',
    'MPG_CREDIT',
    (SELECT id FROM master_data WHERE short_name = 'MPG'),
    (SELECT id FROM master_data WHERE short_name = 'ACT')
) ON CONFLICT (short_name) DO NOTHING;

-- 2. Actualizar parent_id de metodos de pago existentes a sus subcategorias contables

-- Efectivo y Contraentrega
UPDATE master_data
SET parent_id = (SELECT id FROM master_data WHERE short_name = 'MPG_CASH')
WHERE short_name IN ('EFE', 'COD');

-- Tarjetas de debito, credito y datafono POS
UPDATE master_data
SET parent_id = (SELECT id FROM master_data WHERE short_name = 'MPG_CARD')
WHERE short_name IN ('TDEB', 'TCRE', 'POSCARD');

-- Transferencias bancarias, billeteras virtuales y criptomonedas
UPDATE master_data
SET parent_id = (SELECT id FROM master_data WHERE short_name = 'MPG_DIGITAL')
WHERE short_name IN ('TRF', 'WAL', 'CRYPTO');

-- Credito directo y fiado
UPDATE master_data
SET parent_id = (SELECT id FROM master_data WHERE short_name = 'MPG_CREDIT')
WHERE short_name IN ('CREINT', 'FIA');
