-- Migracion Global V3: Indices de Geolocalizacion GiST para Outlets
CREATE INDEX IF NOT EXISTS idx_outlet_location_geog ON outlet USING GIST ((location::geography));
CREATE INDEX IF NOT EXISTS idx_outlet_status ON outlet (status_id);
