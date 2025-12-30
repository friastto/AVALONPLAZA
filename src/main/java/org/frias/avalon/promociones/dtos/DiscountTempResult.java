package org.frias.avalon.promociones.dtos;

import java.math.BigDecimal;

public record DiscountTempResult(
        BigDecimal discount
        , String description
        , BigDecimal priceFinal
) {
}
