package org.frias.avalon.domain.inventory.Producto.modules.adminsaas.services.interfaces;


import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;

public interface ProductoServiceEcommerce {
    ProductResponseDto findById(Long id);
}
