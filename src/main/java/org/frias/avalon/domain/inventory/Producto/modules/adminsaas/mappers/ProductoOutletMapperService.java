package org.frias.avalon.domain.inventory.Producto.modules.adminsaas.mappers;

import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductOutlet;

public interface ProductoOutletMapperService {

    ProductResponseDto toDto(ProductOutlet productOutlet);
}
