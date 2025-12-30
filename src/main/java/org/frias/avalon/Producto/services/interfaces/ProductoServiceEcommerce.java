package org.frias.avalon.Producto.services.interfaces;


import org.frias.avalon.Producto.dtos.ProductRequestCreate;
import org.frias.avalon.Producto.dtos.ProductResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface ProductoServiceEcommerce {
    ProductResponseDto findById(Long id);
}
