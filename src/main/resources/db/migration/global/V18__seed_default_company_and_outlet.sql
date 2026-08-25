-- Migracion Global V18: Seed por defecto para company 1 y outlet 4 en entornos limpios
INSERT INTO company (id, nit, name, status_id, created_at)
VALUES (1, '900000000-1', 'Empresa Avalon Test', (SELECT id FROM master_data WHERE short_name = 'ACT' LIMIT 1), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO outlet (id, code, company_id, name, address, status_id, created_at)
VALUES (4, 'OUTLET_TEST_4', 1, 'Tienda Avalon Outlet 4', 'Calle Principal 123', (SELECT id FROM master_data WHERE short_name = 'ACT' LIMIT 1), NOW())
ON CONFLICT (id) DO NOTHING;

SELECT setval('company_id_seq', GREATEST((SELECT MAX(id) FROM company), 1));
SELECT setval('outlet_id_seq', GREATEST((SELECT MAX(id) FROM outlet), 4));
