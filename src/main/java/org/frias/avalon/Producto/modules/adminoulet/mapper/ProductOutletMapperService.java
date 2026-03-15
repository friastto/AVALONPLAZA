package org.frias.avalon.Producto.modules.adminoulet.mapper;

import org.frias.avalon.Producto.modules.adminoulet.ProductOutletResponseDto;
import org.frias.avalon.Producto.modules.adminsaas.entities.ProductOutlet;

public interface ProductOutletMapperService {

    ProductOutletResponseDto toDto(ProductOutlet productOutlet);



}
