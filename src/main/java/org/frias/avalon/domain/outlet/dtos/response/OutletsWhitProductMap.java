package org.frias.avalon.domain.outlet.dtos.response;

import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductResponseMap;

import java.util.List;

public record OutletsWhitProductMap(
        Long id,
        String name,
        Double lat,
        Double lng,
        List<ProductResponseMap> products
) {
}
