package org.frias.avalon.ventas.dtos;


import org.frias.avalon.Producto.modules.adminsaas.dtos.ProductResponseDetailDto;

import java.math.BigDecimal;


public record SaleDetailResponseDto(
    Long id,

    String quantity,

    BigDecimal unitPrice,



    BigDecimal subTotal,

    ProductResponseDetailDto product
){
}
