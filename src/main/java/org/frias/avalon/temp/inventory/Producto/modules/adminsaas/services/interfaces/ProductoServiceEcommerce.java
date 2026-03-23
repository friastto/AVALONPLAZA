package org.frias.avalon.temp.inventory.Producto.modules.adminsaas.services.interfaces;


import org.frias.avalon.temp.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;

public interface ProductoServiceEcommerce {
    ProductResponseDto findById(Long id);
}
