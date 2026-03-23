package org.frias.avalon.temp.inventory.Producto.modules.adminsaas.mappers;

import org.frias.avalon.temp.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.temp.inventory.Producto.modules.adminsaas.entities.Product;

public interface ProductoMapperService {

    ProductResponseDto toDto(Product product);
}
