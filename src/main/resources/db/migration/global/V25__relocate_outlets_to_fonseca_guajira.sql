-- Migracion Global V25: Reubicacion y distribucion de tiendas en Fonseca, La Guajira
-- Modifica nombres, direcciones y coordenadas geograficas de las 40 tiendas para cobertura en Fonseca

UPDATE company SET name = 'SUPERTIENDAS LA GUAJIRA S.A.S' WHERE id = 9;
UPDATE company SET name = 'ALMACENES MERCAMAS GUAJIRA LTDA' WHERE id = 10;

-- Tiendas Empresa 1: MAGAZINE S.A.S
UPDATE outlet SET 
    name = 'SUPER TIENDA MAGAZINE CENTRO', 
    address = 'Calle 13 # 18-20, Centro, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8525, 10.8865), 4326) 
WHERE id = 1;

UPDATE outlet SET 
    name = 'MAGAZINE ALTO PRADO', 
    address = 'Calle 16 # 15-30, Barrio Alto Prado, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8505, 10.8912), 4326) 
WHERE id = 2;

UPDATE outlet SET 
    name = 'MAGAZINE BRISAS DEL RANCHERIA', 
    address = 'Carrera 20 # 8-15, Barrio Brisas, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8540, 10.8805), 4326) 
WHERE id = 3;

-- Tiendas Empresa 2: SUPERMERCADOS DEL SUR S.A.
UPDATE outlet SET 
    name = 'DEL SUR PLAZA SIMON BOLIVAR', 
    address = 'Calle 12 # 18-08, Plaza Principal, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8518, 10.8858), 4326) 
WHERE id = 4;

UPDATE outlet SET 
    name = 'DEL SUR EL CARMEN', 
    address = 'Carrera 19 # 14-25, Barrio El Carmen, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8565, 10.8872), 4326) 
WHERE id = 5;

UPDATE outlet SET 
    name = 'DEL SUR CARAQUITA', 
    address = 'Calle 11 # 23-18, Barrio Caraquita, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8465, 10.8840), 4326) 
WHERE id = 6;

UPDATE outlet SET 
    name = 'DEL SUR 12 DE OCTUBRE', 
    address = 'Calle 17 # 19-30, Barrio 12 de Octubre, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8535, 10.8925), 4326) 
WHERE id = 7;

UPDATE outlet SET 
    name = 'DEL SUR SAN AGUSTIN', 
    address = 'Carrera 17 # 15-40, Barrio San Agustin, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8512, 10.8885), 4326) 
WHERE id = 8;

UPDATE outlet SET 
    name = 'DEL SUR EXPRESS EFRAIN MEDINA', 
    address = 'Calle 14 # 24-12, Barrio Efrain Medina, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8610, 10.8860), 4326) 
WHERE id = 9;

-- Tiendas Empresa 3: FRUVER LA COLMENA S.A.S
UPDATE outlet SET 
    name = 'LA COLMENA MERCADO CENTRAL', 
    address = 'Carrera 19 # 13-10, Sector Mercado, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8538, 10.8870), 4326) 
WHERE id = 10;

UPDATE outlet SET 
    name = 'LA COLMENA CALLE REAL', 
    address = 'Calle 13 # 19-45, Calle Real, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8550, 10.8862), 4326) 
WHERE id = 11;

UPDATE outlet SET 
    name = 'LA COLMENA 15 DE DICIEMBRE', 
    address = 'Calle 18 # 16-18, Barrio 15 de Diciembre, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8495, 10.8935), 4326) 
WHERE id = 12;

UPDATE outlet SET 
    name = 'LA COLMENA EL CAMPO', 
    address = 'Carrera 22 # 9-35, Barrio El Campo, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8585, 10.8812), 4326) 
WHERE id = 13;

-- Tiendas Empresa 4: MINIMARKET EL TREBOL LTDA
UPDATE outlet SET 
    name = 'EL TREBOL VILLA LUZ', 
    address = 'Calle 15 # 14-20, Barrio Villa Luz, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8480, 10.8895), 4326) 
WHERE id = 14;

UPDATE outlet SET 
    name = 'EL TREBOL TRONCAL ORIENTE', 
    address = 'Carretera Nacional # 18-50, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8450, 10.8845), 4326) 
WHERE id = 15;

-- Tiendas Empresa 5: DISTRIBUIDORA Y LACTEOS EL ROSAL
UPDATE outlet SET 
    name = 'EL ROSAL CENTRO FONSECA', 
    address = 'Carrera 18 # 12-30, Centro, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8528, 10.8852), 4326) 
WHERE id = 16;

UPDATE outlet SET 
    name = 'EL ROSAL LOS CERROS', 
    address = 'Calle 19 # 17-40, Barrio Los Cerros, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8510, 10.8940), 4326) 
WHERE id = 17;

UPDATE outlet SET 
    name = 'EL ROSAL LA FLORESTA', 
    address = 'Carrera 15 # 16-15, Barrio La Floresta, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8475, 10.8905), 4326) 
WHERE id = 18;

UPDATE outlet SET 
    name = 'EL ROSAL SAN JOSE', 
    address = 'Calle 9 # 18-22, Barrio San Jose, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8525, 10.8795), 4326) 
WHERE id = 19;

UPDATE outlet SET 
    name = 'EL ROSAL LAS DELICIAS', 
    address = 'Carrera 23 # 12-10, Barrio Las Delicias, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8595, 10.8850), 4326) 
WHERE id = 20;

-- Tiendas Empresa 6: PANADERIA Y REPOSTERIA GOURMET S.A.S
UPDATE outlet SET 
    name = 'PANADERIA GOURMET FONSECA', 
    address = 'Calle 13 # 18-12, Parque Central, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8520, 10.8860), 4326) 
WHERE id = 21;

-- Tiendas Empresa 7: DROGUERIA Y CONVENIENCIA LA ECONOMICA
UPDATE outlet SET 
    name = 'LA ECONOMICA PLAZA CENTRAL', 
    address = 'Calle 12 # 18-25, Centro, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8530, 10.8855), 4326) 
WHERE id = 22;

UPDATE outlet SET 
    name = 'LA ECONOMICA EL CARMEN', 
    address = 'Carrera 20 # 14-40, Barrio El Carmen, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8575, 10.8875), 4326) 
WHERE id = 23;

UPDATE outlet SET 
    name = 'LA ECONOMICA ALTO PRADO', 
    address = 'Calle 16 # 14-18, Barrio Alto Prado, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8490, 10.8918), 4326) 
WHERE id = 24;

UPDATE outlet SET 
    name = 'LA ECONOMICA SALIDA DISTRACCION', 
    address = 'Carretera Troncal km 1, Salida a Distraccion, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8635, 10.8820), 4326) 
WHERE id = 25;

UPDATE outlet SET 
    name = 'LA ECONOMICA 12 DE OCTUBRE', 
    address = 'Carrera 21 # 16-20, Barrio 12 de Octubre, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8550, 10.8915), 4326) 
WHERE id = 26;

UPDATE outlet SET 
    name = 'LA ECONOMICA BRISAS', 
    address = 'Calle 8 # 19-15, Barrio Brisas, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8535, 10.8788), 4326) 
WHERE id = 27;

UPDATE outlet SET 
    name = 'LA ECONOMICA HOSPITAL SAN AGUSTIN', 
    address = 'Carrera 16 # 11-45, Cerca al Hospital, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8505, 10.8835), 4326) 
WHERE id = 28;

-- Tiendas Empresa 8: CENTRO AGROPECUARIO SAN JORGE
UPDATE outlet SET 
    name = 'SAN JORGE AGROPECUARIO FONSECA', 
    address = 'Calle 11 # 21-10, Salida a Barrancas, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8440, 10.8830), 4326) 
WHERE id = 29;

-- Tiendas Empresa 9: SUPERTIENDAS LA GUAJIRA S.A.S
UPDATE outlet SET 
    name = 'SUPERTIENDAS GUAJIRA CENTRO', 
    address = 'Carrera 18 # 12-50, Centro, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8532, 10.8861), 4326) 
WHERE id = 30;

UPDATE outlet SET 
    name = 'SUPERTIENDAS GUAJIRA SUR', 
    address = 'Calle 10 # 18-35, Sector Sur, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8522, 10.8815), 4326) 
WHERE id = 31;

UPDATE outlet SET 
    name = 'SUPERTIENDAS EL RETIRO', 
    address = 'Carrera 22 # 15-12, Barrio El Retiro, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8580, 10.8890), 4326) 
WHERE id = 32;

UPDATE outlet SET 
    name = 'SUPERTIENDAS BUENAVISTA', 
    address = 'Calle 17 # 17-40, Barrio Buenavista, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8520, 10.8928), 4326) 
WHERE id = 33;

UPDATE outlet SET 
    name = 'SUPERTIENDAS EL SALADO', 
    address = 'Carrera 16 # 9-25, Sector El Salado, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8490, 10.8802), 4326) 
WHERE id = 34;

UPDATE outlet SET 
    name = 'SUPERTIENDAS EL PORTAL', 
    address = 'Calle 14 # 16-18, Urbanizacion El Portal, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8495, 10.8880), 4326) 
WHERE id = 35;

UPDATE outlet SET 
    name = 'SUPERTIENDAS PRIMERO DE JULIO', 
    address = 'Carrera 19 # 17-30, Barrio Primero de Julio, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8545, 10.8930), 4326) 
WHERE id = 36;

UPDATE outlet SET 
    name = 'SUPERTIENDAS VILLA FONSECA', 
    address = 'Calle 18 # 18-10, Villa Fonseca, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8530, 10.8938), 4326) 
WHERE id = 37;

-- Tiendas Empresa 10: ALMACENES MERCAMAS GUAJIRA LTDA
UPDATE outlet SET 
    name = 'MERCAMAS FONSECA CALLE REAL', 
    address = 'Calle 13 # 17-25, Calle Real, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8510, 10.8863), 4326) 
WHERE id = 38;

UPDATE outlet SET 
    name = 'MERCAMAS FONSECA PLAZA', 
    address = 'Calle 12 # 19-10, Frente a Plaza Simon Bolivar, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8545, 10.8856), 4326) 
WHERE id = 39;

UPDATE outlet SET 
    name = 'MERCAMAS FONSECA TRONCAL', 
    address = 'Avenida Troncal # 14-80, Salida Norte, Fonseca', 
    location = ST_SetSRID(ST_Point(-72.8455, 10.8875), 4326) 
WHERE id = 40;
