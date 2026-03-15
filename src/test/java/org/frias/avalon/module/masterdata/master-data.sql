INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES
    ('TYPE_ESTADOS', 'ROOTSTS', null, 3);

INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES
    ('ESTADOS_GENERALES', 'STSGEN',(select id from master_data where short_name = 'ROOTSTS' ), 3);

INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES
    ( 'ACTIVO',  'ACT', (select id from master_data where short_name ='STSGEN' ), 3);


INSERT INTO master_data (full_name, short_name, parent_id, status_id)
VALUES
    ('INACTIVO',  'INA',(select id from master_data where short_name = 'STSGEN' ), ( select id from master_data where short_name = 'ACT')),
    ( 'SUSPENDIDO',  'SUS', (select id from master_data where short_name = 'STSGEN' ), (select id from master_data where short_name = 'ACT')),
    ('ELIMINADO',  'DEL', (select id from master_data where short_name = 'STSGEN' ), ( select id from master_data where short_name = 'ACT'))



