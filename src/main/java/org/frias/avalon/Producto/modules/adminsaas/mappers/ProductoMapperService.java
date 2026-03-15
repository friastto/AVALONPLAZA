package org.frias.avalon.Producto.modules.adminsaas.mappers;

import org.frias.avalon.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.Producto.modules.adminsaas.entities.Product;

public interface ProductoMapperService {

    ProductResponseDto toDto(Product product);
}
