package org.frias.avalon.Producto.modules.adminsaas.services.interfaces;


import org.frias.avalon.Producto.modules.adminsaas.dtos.ProductResponseDto;

public interface ProductoServiceEcommerce {
    ProductResponseDto findById(Long id);
}
