package org.frias.avalon.temp.empresasucursal.sucursal.dtos;

import org.frias.avalon.temp.inventory.Producto.modules.adminsaas.dtos.ProductResponseMap;

import java.util.List;

public record OutletsWhitProductResponseMap(
        Long id,
        String name,
        Double lat,
        Double lng,
        List<ProductResponseMap> products
) {
}
