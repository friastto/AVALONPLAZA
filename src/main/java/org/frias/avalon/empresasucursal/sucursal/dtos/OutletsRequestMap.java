package org.frias.avalon.empresasucursal.sucursal.dtos;

public record OutletsRequestMap(
        String query,
        Double lat,
        Double lng,
        int radius


) {
}
