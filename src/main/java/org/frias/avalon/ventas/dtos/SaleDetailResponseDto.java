package org.frias.avalon.ventas.dtos;


import org.frias.avalon.Producto.dtos.ProductResponseDetailDto;
import org.frias.avalon.Producto.entities.Product;
import org.frias.avalon.ventas.entities.Sale;

import java.math.BigDecimal;


public record SaleDetailResponseDto(
    Long id,

    String quantity,

    BigDecimal unitPrice,



    BigDecimal subTotal,

    ProductResponseDetailDto product
){
}
