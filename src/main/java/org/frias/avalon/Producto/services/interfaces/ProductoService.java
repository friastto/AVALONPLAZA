package org.frias.avalon.Producto.services.interfaces;


import org.frias.avalon.Producto.dtos.ProductRequestCreate;
import org.frias.avalon.Producto.dtos.ProductResponseDto;
import org.frias.avalon.Producto.entities.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductoService {
    Product searchById(Long id);
    BigDecimal calculatePrice(Long productId, String identity);

    ProductResponseDto save(ProductRequestCreate request);



    ProductResponseDto findByCodeBar(String codeBar);

    ProductResponseDto searchByName(String name);

    List<ProductResponseDto> findAll();
}
