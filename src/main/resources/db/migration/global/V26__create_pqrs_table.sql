-- V26__create_pqrs_table.sql
-- Modulo de PQRS (Peticiones, Quejas, Reclamos y Sugerencias) a nivel de plataforma Avalon

CREATE TABLE IF NOT EXISTS public.pqrs (
    id BIGSERIAL PRIMARY KEY,
    ticket_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT REFERENCES public.user_avalon(id) ON DELETE SET NULL,
    order_id BIGINT REFERENCES public.orders(id) ON DELETE SET NULL,
    store_id BIGINT REFERENCES public.outlet(id) ON DELETE SET NULL,
    type_code VARCHAR(20) NOT NULL,
    status_code VARCHAR(20) NOT NULL DEFAULT 'PEN',
    subject VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    contact_email VARCHAR(150),
    contact_phone VARCHAR(50),
    admin_notes TEXT,
    responded_by_user_id BIGINT REFERENCES public.user_avalon(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pqrs_status ON public.pqrs(status_code);
CREATE INDEX IF NOT EXISTS idx_pqrs_type ON public.pqrs(type_code);
CREATE INDEX IF NOT EXISTS idx_pqrs_ticket ON public.pqrs(ticket_number);
CREATE INDEX IF NOT EXISTS idx_pqrs_created ON public.pqrs(created_at DESC);
