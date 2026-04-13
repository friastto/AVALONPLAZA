package org.frias.avalon.domain.product.application.services.interfaces;


import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;

public interface ProductoServiceEcommerce {
    ProductResponseDto findById(Long id);
}
