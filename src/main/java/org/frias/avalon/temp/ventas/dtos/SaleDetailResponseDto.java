package org.frias.avalon.temp.ventas.dtos;


import org.frias.avalon.temp.inventory.Producto.modules.adminsaas.dtos.ProductResponseDetailDto;

import java.math.BigDecimal;


public record SaleDetailResponseDto(
    Long id,

    String quantity,

    BigDecimal unitPrice,



    BigDecimal subTotal,

    ProductResponseDetailDto product
){
}
