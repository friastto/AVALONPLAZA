package org.frias.avalon.domain.sale.dtos;


import java.math.BigDecimal;


public record SaleDetailRequest (
    String quantity,

    BigDecimal unitPrice,

    Long productId
){
}
