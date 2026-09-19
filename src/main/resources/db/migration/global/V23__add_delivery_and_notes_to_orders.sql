-- V23__add_delivery_and_notes_to_orders.sql
-- Agregar campos de entrega (delivery_type, delivery_address, delivery_fee) y notas/observaciones

ALTER TABLE public.omnichannel_orders ADD COLUMN IF NOT EXISTS delivery_type VARCHAR(30) DEFAULT 'PICKUP';
ALTER TABLE public.omnichannel_orders ADD COLUMN IF NOT EXISTS delivery_address VARCHAR(255);
ALTER TABLE public.omnichannel_orders ADD COLUMN IF NOT EXISTS delivery_fee NUMERIC(15, 2) DEFAULT 0.00;
ALTER TABLE public.omnichannel_orders ADD COLUMN IF NOT EXISTS notes TEXT;

ALTER TABLE public.omnichannel_order_items ADD COLUMN IF NOT EXISTS notes TEXT;
