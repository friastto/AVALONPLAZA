-- Limpiar tablas
DELETE FROM master_data;

-- 1. Estados Base (Necesarios para que el sistema funcione)
-- ID 1
INSERT INTO master_data (id, full_name, short_name, parent_id, status_id)
VALUES (1, 'ESTADO_RAIZ', 'ROOT_STS', NULL, 1);

-- ID 2 (ACTIVO) - Este es el que busca tu código 'ACT'
INSERT INTO master_data (id, full_name, short_name, parent_id, status_id)
VALUES (2, 'ACTIVO', 'ACT', 1, 2);

-- ID 3 (INACTIVO)
INSERT INTO master_data (id, full_name, short_name, parent_id, status_id)
VALUES (3, 'INACTIVO', 'INA', 1, 2);


-- 2. Jerarquía de Prueba (Para probar getRootBranch y Children)
-- ID 10: Root de Categoría
INSERT INTO master_data (id, full_name, short_name, parent_id, status_id)
VALUES (10, 'CATEGORIA_PRODUCTO', 'CAT_PROD', NULL, 2);

-- ID 11: Electrónica (Hijo de CAT_PROD)
INSERT INTO master_data (id, full_name, short_name, parent_id, status_id)
VALUES (11, 'ELECTRONICA', 'ELEC', 10, 2);

-- ID 12: Ropa (Hijo de CAT_PROD)
INSERT INTO master_data (id, full_name, short_name, parent_id, status_id)
VALUES (12, 'ROPA', 'ROPA', 10, 2);

-- ID 13: Celulares (Hijo de ELEC)
INSERT INTO master_data (id, full_name, short_name, parent_id, status_id)
VALUES (13, 'CELULARES', 'CEL', 11, 2);

-- ID 14: Laptops (Hijo de ELEC) - INACTIVO
INSERT INTO master_data (id, full_name, short_name, parent_id, status_id)
VALUES (14, 'LAPTOPS', 'LAP', 11, 3); -- Status 3 es INACTIVO