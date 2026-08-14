-- =============================================================================
-- V12: SEED MASTER DATA TREE AND DEFAULT SUPER ADMIN USER 'frias'
-- =============================================================================

-- 1. Master Data Seed Nodes (156 Nodes)
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('ACTIVO', 'ACT', NULL, NULL) ON CONFLICT (short_name) DO NOTHING;
UPDATE master_data SET status_id = (SELECT id FROM master_data WHERE short_name = 'ACT') WHERE short_name = 'ACT' AND status_id IS NULL;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_ESTADOS', 'ROOTSTS', NULL, (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_GENERO', 'GEN', NULL, (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_IDENTIFICACION', 'IDENT', NULL, (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_ALIMENTOS', 'ALIM', NULL, (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_BEBIDAS', 'BEBID', NULL, (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_UNIDAD_MEDIDA', 'UNIT', NULL, (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_ROL', 'ROL', NULL, (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_PROMOTION', 'PROM', NULL, (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_METODO_DE_PAGO', 'MPG', NULL, (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_STAFF', 'STAFF', NULL, (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('ESTADOS_GENERALS', 'STSGEN', (SELECT id FROM master_data WHERE short_name = 'ROOTSTS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('STS_USERS', 'USR_STS', (SELECT id FROM master_data WHERE short_name = 'ROOTSTS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('STS_LOGISTICS', 'STSLOGIS', (SELECT id FROM master_data WHERE short_name = 'ROOTSTS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('STS_REVISION', 'STSREV', (SELECT id FROM master_data WHERE short_name = 'ROOTSTS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('STS_SISTEMA', 'STSSYS', (SELECT id FROM master_data WHERE short_name = 'ROOTSTS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('MASCULINO', 'M', (SELECT id FROM master_data WHERE short_name = 'GEN'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('FEMENINO', 'F', (SELECT id FROM master_data WHERE short_name = 'GEN'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('NO_BINARIO', 'NBIN', (SELECT id FROM master_data WHERE short_name = 'GEN'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('PREFIERO_NO_DECIRLO', 'SINDET', (SELECT id FROM master_data WHERE short_name = 'GEN'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('CEDULA_CIUDADANIA', 'CC', (SELECT id FROM master_data WHERE short_name = 'IDENT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TARJETA_IDENTIDAD', 'TI', (SELECT id FROM master_data WHERE short_name = 'IDENT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('CEDULA_EXTRANJERIA', 'CE', (SELECT id FROM master_data WHERE short_name = 'IDENT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('PASAPORTE', 'PAS', (SELECT id FROM master_data WHERE short_name = 'IDENT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('REGISTRO_CIVIL', 'RC', (SELECT id FROM master_data WHERE short_name = 'IDENT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('NIT', 'NIT', (SELECT id FROM master_data WHERE short_name = 'IDENT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('LICENCIA_CONDUCCION', 'LICON', (SELECT id FROM master_data WHERE short_name = 'IDENT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('CARNES', 'CARN', (SELECT id FROM master_data WHERE short_name = 'ALIM'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('CARBOHIDRATOS', 'CARB', (SELECT id FROM master_data WHERE short_name = 'ALIM'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('VEGETALES', 'VEGE', (SELECT id FROM master_data WHERE short_name = 'ALIM'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('FRUTAS', 'FRUT', (SELECT id FROM master_data WHERE short_name = 'ALIM'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('LACTEOS', 'LACT', (SELECT id FROM master_data WHERE short_name = 'ALIM'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('ACEITES_Y_GRASAS', 'ACEIGRA', (SELECT id FROM master_data WHERE short_name = 'ALIM'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('PESCADOS_Y_MARISCOS', 'PESMAR', (SELECT id FROM master_data WHERE short_name = 'ALIM'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('GRANOS', 'GRAN', (SELECT id FROM master_data WHERE short_name = 'ALIM'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('CONDIMENTOS_Y_ESPECIES', 'CONDESP', (SELECT id FROM master_data WHERE short_name = 'ALIM'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('AGUAS', 'AGUA', (SELECT id FROM master_data WHERE short_name = 'BEBID'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('JUGOS', 'JUGO', (SELECT id FROM master_data WHERE short_name = 'BEBID'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('REFRESCOS_Y_GACEOSAS', 'REFRIGA', (SELECT id FROM master_data WHERE short_name = 'BEBID'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('INFUCIONES', 'INFUCIONES', (SELECT id FROM master_data WHERE short_name = 'BEBID'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('IZOTONICAS_Y_ENERGIZANTES', 'IZODEP', (SELECT id FROM master_data WHERE short_name = 'BEBID'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('ALCOLICAS_DESTILADAS', 'ALCOL', (SELECT id FROM master_data WHERE short_name = 'BEBID'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('FERMENTADAS', 'FERM', (SELECT id FROM master_data WHERE short_name = 'BEBID'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('UNIDAD', 'UND', (SELECT id FROM master_data WHERE short_name = 'UNIT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_MASA', 'MASA', (SELECT id FROM master_data WHERE short_name = 'UNIT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_VOLUMEN', 'VOL', (SELECT id FROM master_data WHERE short_name = 'UNIT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_SISTEM', 'SISTEM', (SELECT id FROM master_data WHERE short_name = 'ROL'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_EMPLOYEE', 'EMP', (SELECT id FROM master_data WHERE short_name = 'ROL'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_COSUMER', 'CONS', (SELECT id FROM master_data WHERE short_name = 'ROL'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_CAMPANAS', 'CAMP', (SELECT id FROM master_data WHERE short_name = 'PROM'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_DESCUENTO_MANUAL', 'DESMAN', (SELECT id FROM master_data WHERE short_name = 'PROM'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_DESCUENTO_EN_VOLUMNEN', 'DESVOL', (SELECT id FROM master_data WHERE short_name = 'PROM'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('EFECTIVO', 'EFE', (SELECT id FROM master_data WHERE short_name = 'MPG'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('CONSUMER', 'CONSMR', (SELECT id FROM master_data WHERE short_name = 'STAFF'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('EMPLOYEE', 'EMPL', (SELECT id FROM master_data WHERE short_name = 'STAFF'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('INACTIVO', 'INA', (SELECT id FROM master_data WHERE short_name = 'STSGEN'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('SUSPENDIDO', 'SUS', (SELECT id FROM master_data WHERE short_name = 'STSGEN'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('ELIMINADO', 'DEL', (SELECT id FROM master_data WHERE short_name = 'STSGEN'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('BLOQUEADO', 'LOKUSER', (SELECT id FROM master_data WHERE short_name = 'USR_STS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('PENDIENTE_VERIFICACION', 'PND', (SELECT id FROM master_data WHERE short_name = 'USR_STS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('VERIFICADO', 'VER', (SELECT id FROM master_data WHERE short_name = 'USR_STS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('BANEADO', 'BAN', (SELECT id FROM master_data WHERE short_name = 'USR_STS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('ANONIMO', 'ANY', (SELECT id FROM master_data WHERE short_name = 'USR_STS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('PENDIENTE', 'PEN', (SELECT id FROM master_data WHERE short_name = 'STSLOGIS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('EN_PROCESO', 'PRO', (SELECT id FROM master_data WHERE short_name = 'STSLOGIS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('COMPLETADO', 'COM', (SELECT id FROM master_data WHERE short_name = 'STSLOGIS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('CANCELADO', 'CAN', (SELECT id FROM master_data WHERE short_name = 'STSLOGIS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('RECHAZADO', 'REC', (SELECT id FROM master_data WHERE short_name = 'STSLOGIS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('BORRADOR', 'DFT', (SELECT id FROM master_data WHERE short_name = 'STSREV'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('EN_REVISION', 'RVW', (SELECT id FROM master_data WHERE short_name = 'STSREV'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('APROBADO', 'APR', (SELECT id FROM master_data WHERE short_name = 'STSREV'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('OBSERVADO', 'OBS', (SELECT id FROM master_data WHERE short_name = 'STSREV'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('SYSTEM_BLOQUEADO', 'LOKSIS', (SELECT id FROM master_data WHERE short_name = 'STSSYS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('ERROR', 'ERR', (SELECT id FROM master_data WHERE short_name = 'STSSYS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('MANTENIMIENTO', 'MNT', (SELECT id FROM master_data WHERE short_name = 'STSSYS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('SINCRONIZADO', 'SYNC', (SELECT id FROM master_data WHERE short_name = 'STSSYS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TONELADAS', 'TON', (SELECT id FROM master_data WHERE short_name = 'MASA'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('KILOGRAMOS', 'KG', (SELECT id FROM master_data WHERE short_name = 'MASA'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('LIBRAS', 'LB', (SELECT id FROM master_data WHERE short_name = 'MASA'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('GRAMOS', 'G', (SELECT id FROM master_data WHERE short_name = 'MASA'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('ONZA', 'OZ', (SELECT id FROM master_data WHERE short_name = 'MASA'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('ARROBA', '@', (SELECT id FROM master_data WHERE short_name = 'MASA'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('QUINTAL_COMERCIAL', 'QQCM', (SELECT id FROM master_data WHERE short_name = 'MASA'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('MILILITROS', 'ML', (SELECT id FROM master_data WHERE short_name = 'VOL'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('CENTILITRO', 'CL', (SELECT id FROM master_data WHERE short_name = 'VOL'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('LITRO', 'L', (SELECT id FROM master_data WHERE short_name = 'VOL'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('METRO_CUBICO', 'M3', (SELECT id FROM master_data WHERE short_name = 'VOL'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('ONZA_LIQUIDA', 'FL', (SELECT id FROM master_data WHERE short_name = 'VOL'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('PINTA', 'PT', (SELECT id FROM master_data WHERE short_name = 'VOL'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('CUATO', 'GT', (SELECT id FROM master_data WHERE short_name = 'VOL'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('GALON', 'GAL', (SELECT id FROM master_data WHERE short_name = 'VOL'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('BARRIL', 'BBL', (SELECT id FROM master_data WHERE short_name = 'VOL'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('ADMINISTRADOR_SISTEMA', 'ADMIN', (SELECT id FROM master_data WHERE short_name = 'SISTEM'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_DIRECTIVO', 'DIREC', (SELECT id FROM master_data WHERE short_name = 'EMP'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_GERENTES', 'GERENTE', (SELECT id FROM master_data WHERE short_name = 'EMP'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_OPERATIVO', 'OPT', (SELECT id FROM master_data WHERE short_name = 'EMP'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_CLIENTE', 'CLIENTE', (SELECT id FROM master_data WHERE short_name = 'CONS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('TYPE_USUARIOS', 'USUARIO', (SELECT id FROM master_data WHERE short_name = 'CONS'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('CYBER_DAY', 'CDAY', (SELECT id FROM master_data WHERE short_name = 'CAMP'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('BLACK_FRIDAY', 'BCKDAYS', (SELECT id FROM master_data WHERE short_name = 'CAMP'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('FREE_DAY', 'FRDAY', (SELECT id FROM master_data WHERE short_name = 'CAMP'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('HOT_SALE', 'HOTSAL', (SELECT id FROM master_data WHERE short_name = 'CAMP'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('CRISMAS_SALE', 'CSALE', (SELECT id FROM master_data WHERE short_name = 'CAMP'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('BACK_SCHOOL', 'BKSCH', (SELECT id FROM master_data WHERE short_name = 'CAMP'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('DESC_PORCENTUAL', 'DESPOR', (SELECT id FROM master_data WHERE short_name = 'DESMAN'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('DESC_MONTO_FIJO', 'DESMONT', (SELECT id FROM master_data WHERE short_name = 'DESMAN'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('PRECIO_ESPECIAL', 'PREESP', (SELECT id FROM master_data WHERE short_name = 'DESMAN'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('DIA_SIN_IVA', 'DAYOFFIVA', (SELECT id FROM master_data WHERE short_name = 'DESMAN'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('COMPRA_2_PAGA_1', '2X1', (SELECT id FROM master_data WHERE short_name = 'DESVOL'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('COMPRA_3_PAGA_2', '2X3', (SELECT id FROM master_data WHERE short_name = 'DESVOL'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('SEGUNDA_UNIDAD_CON_DESCUENTO', '2DAPCT', (SELECT id FROM master_data WHERE short_name = 'DESVOL'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('LLEVA_X_PAGA_Y', 'LLEVXPGY', (SELECT id FROM master_data WHERE short_name = 'DESVOL'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('ADMIN_INFRAESTRUCTURA_TI', 'ADMINTI', (SELECT id FROM master_data WHERE short_name = 'ADMIN'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('ADMIN_BASE_DE_DATOS', 'ADMINBD', (SELECT id FROM master_data WHERE short_name = 'ADMIN'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('ADMIN_SEGURIDAD_TI', 'ADMINSEGTI', (SELECT id FROM master_data WHERE short_name = 'ADMIN'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('FUNDADOR', 'FUN', (SELECT id FROM master_data WHERE short_name = 'DIREC'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('DIRECTOR_OPERATIVO', 'DIROP', (SELECT id FROM master_data WHERE short_name = 'DIREC'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('DIRECTOR_REGIONAL', 'DIRREG', (SELECT id FROM master_data WHERE short_name = 'DIREC'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('SOCIO', 'SOCIO', (SELECT id FROM master_data WHERE short_name = 'DIREC'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('GERENTE_GENERAL', 'GERGEN', (SELECT id FROM master_data WHERE short_name = 'GERENTE'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('GERENTE_TURNO', 'GERTUR', (SELECT id FROM master_data WHERE short_name = 'GERENTE'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('GERENTE_PISO', 'GERPI', (SELECT id FROM master_data WHERE short_name = 'GERENTE'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('GERENTE_INVERCION_Y_ALMACEN', 'GERINVAL', (SELECT id FROM master_data WHERE short_name = 'GERENTE'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('GERENTE_SERVICIO_AL_CLIENTE', 'GERSERVCLI', (SELECT id FROM master_data WHERE short_name = 'GERENTE'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('GERENTE_OPERACION_LOGISTICA', 'GEROPLOGIS', (SELECT id FROM master_data WHERE short_name = 'GERENTE'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('CAJERO_PRINCIPAL', 'CJPRINCIPAL', (SELECT id FROM master_data WHERE short_name = 'OPT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('CAJERO_TURNO', 'CJTURNO', (SELECT id FROM master_data WHERE short_name = 'OPT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('VENDEDOR_PISO', 'VNDPISO', (SELECT id FROM master_data WHERE short_name = 'OPT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('VENDEDOR_ESPECIALIZADO', 'VNDESP', (SELECT id FROM master_data WHERE short_name = 'OPT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('VENDEDOR_PREVENTISTA', 'VNDPREVEN', (SELECT id FROM master_data WHERE short_name = 'OPT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('CAJERO_PUNTO_ENTREGA', 'CJPUNENTRY', (SELECT id FROM master_data WHERE short_name = 'OPT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('SUPERVISOR', 'SUPERVISOR', (SELECT id FROM master_data WHERE short_name = 'OPT'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('ESTANDAR', 'CSTNDR', (SELECT id FROM master_data WHERE short_name = 'CLIENTE'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('FRECUENTE', 'CFREC', (SELECT id FROM master_data WHERE short_name = 'CLIENTE'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('VIP', 'CVIP', (SELECT id FROM master_data WHERE short_name = 'CLIENTE'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('PREMIUM', 'CPREM', (SELECT id FROM master_data WHERE short_name = 'CLIENTE'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('CORPORATIVO', 'CCORP', (SELECT id FROM master_data WHERE short_name = 'CLIENTE'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('SUBCRIPCION', 'CSUBS', (SELECT id FROM master_data WHERE short_name = 'CLIENTE'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('REFERIDO', 'CREF', (SELECT id FROM master_data WHERE short_name = 'CLIENTE'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('VISITANTE_WEB', 'VISWEB', (SELECT id FROM master_data WHERE short_name = 'USUARIO'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('VISITANTE_APP', 'VISAPP', (SELECT id FROM master_data WHERE short_name = 'USUARIO'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('PROSPECTO', 'VISPROSPEC', (SELECT id FROM master_data WHERE short_name = 'USUARIO'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('LOCAL', 'LOCAL', (SELECT id FROM master_data WHERE short_name = 'USUARIO'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('INVITADO', 'INVITADO', (SELECT id FROM master_data WHERE short_name = 'USUARIO'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;
INSERT INTO master_data (full_name, short_name, parent_id, status_id) VALUES ('USER_ANONIMO', 'USANONIMO', (SELECT id FROM master_data WHERE short_name = 'USUARIO'), (SELECT id FROM master_data WHERE short_name = 'ACT')) ON CONFLICT (short_name) DO NOTHING;

-- 2. Default Person Record for Super Admin
INSERT INTO person (number_id, name, last_name, address, identification_id, sex_id, phone_number, email, status_id, created_at)
VALUES (
    '1122415280', 
    'Roberto', 
    'Frias', 
    'Av. Principal 123', 
    (SELECT id FROM master_data WHERE short_name = 'CC'), 
    (SELECT id FROM master_data WHERE short_name = 'M'), 
    3001234567, 
    'ntnfrias@gmail.com', 
    (SELECT id FROM master_data WHERE short_name = 'ACT'), 
    NOW()
) ON CONFLICT (identification_id, number_id) DO NOTHING;

-- 3. Default Super Admin User 'frias' (Password: 123456)
INSERT INTO user_avalon (person_id, user_name, hash_salt, hash_password, status_id, created_at)
VALUES (
    (SELECT id FROM person WHERE number_id = '1122415280'),
    'frias',
    'frias_salt',
    '$2a$10$w0fS7yZpM6Q58hA5.g6K8uC2/JqZ1xZ0g6V8uC2/JqZ1xZ0g6V8uC',
    (SELECT id FROM master_data WHERE short_name = 'ACT'),
    NOW()
) ON CONFLICT (user_name) DO NOTHING;

-- 4. Default Role Assignments for 'frias' (ROLE_ADMINTI & ROLE_ADMIN)
INSERT INTO role_assignment (user_id, role_id, outlet_id, status, created_at)
SELECT 
    (SELECT id FROM user_avalon WHERE user_name = 'frias'),
    (SELECT id FROM master_data WHERE short_name = 'ADMINTI'),
    NULL,
    (SELECT id FROM master_data WHERE short_name = 'ACT'),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM role_assignment WHERE user_id = (SELECT id FROM user_avalon WHERE user_name = 'frias') AND role_id = (SELECT id FROM master_data WHERE short_name = 'ADMINTI')
);

INSERT INTO role_assignment (user_id, role_id, outlet_id, status, created_at)
SELECT 
    (SELECT id FROM user_avalon WHERE user_name = 'frias'),
    (SELECT id FROM master_data WHERE short_name = 'ADMIN'),
    NULL,
    (SELECT id FROM master_data WHERE short_name = 'ACT'),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM role_assignment WHERE user_id = (SELECT id FROM user_avalon WHERE user_name = 'frias') AND role_id = (SELECT id FROM master_data WHERE short_name = 'ADMIN')
);
