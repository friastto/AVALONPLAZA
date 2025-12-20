package org.frias.avalon.Producto.services.interfaces;


import org.frias.avalon.Producto.dtos.ProductRequestCreate;
import org.frias.avalon.Producto.dtos.ProductResponseDto;

import java.math.BigDecimal;

public interface ProductoService {
    BigDecimal calculatePrice(Long productId, String identity);

    ProductResponseDto save(ProductRequestCreate request);

}
