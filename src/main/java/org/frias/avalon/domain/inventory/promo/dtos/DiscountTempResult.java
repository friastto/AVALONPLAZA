package org.frias.avalon.domain.inventory.promo.dtos;

import java.math.BigDecimal;

public record DiscountTempResult(
        BigDecimal discount
        , String description
        , BigDecimal priceFinal
) {
}
