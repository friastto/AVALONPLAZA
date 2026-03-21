package org.frias.avalon.sales.ventas.dtos;


import java.math.BigDecimal;


public record SaleDetailRequest (
    String quantity,

    BigDecimal unitPrice,

    Long productId
){
}
