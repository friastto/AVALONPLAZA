package org.frias.avalon.ventas.dtos;


import java.math.BigDecimal;


public record SaleDetailRequest (
    String quantity,

    BigDecimal unitPrice,

    Long productId
){
}
