package org.frias.avalon.temp.empresasucursal.sucursal.dtos;

public record OutletsRequestMap(
        String query,
        Double lat,
        Double lng,
        int radius


) {
}
