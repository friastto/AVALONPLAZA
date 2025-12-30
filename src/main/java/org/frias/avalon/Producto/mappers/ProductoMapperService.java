package org.frias.avalon.Producto.mappers;

import org.frias.avalon.Producto.dtos.ProductResponseDto;
import org.frias.avalon.Producto.entities.Product;

public interface ProductoMapperService {

    ProductResponseDto toDto(Product product);
}
