package org.frias.avalon.domain.inventory.Producto.modules.adminoulet.mapper;

import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.ProductOutletResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductOutlet;

public interface ProductOutletMapperService {

    ProductOutletResponseDto toDto(ProductOutlet productOutlet);



}
