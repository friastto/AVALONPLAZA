package org.frias.avalon.Producto.services.interfaces;


import org.frias.avalon.Producto.dtos.ProductResponseDto;

public interface ProductoServiceEcommerce {
    ProductResponseDto findById(Long id);
}
