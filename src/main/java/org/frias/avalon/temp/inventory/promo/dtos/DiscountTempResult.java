package org.frias.avalon.temp.inventory.promo.dtos;

import java.math.BigDecimal;

public record DiscountTempResult(
        BigDecimal discount
        , String description
        , BigDecimal priceFinal
) {
}
