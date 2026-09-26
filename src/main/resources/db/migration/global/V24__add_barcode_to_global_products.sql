-- Migracion Global V24: Anadir columna barcode a la tabla public.products (Nivel 1)
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS barcode VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_products_barcode ON public.products (barcode);
