package org.frias.avalon.promociones.factory.oters.components;

import org.frias.avalon.promociones.dtos.DiscountTempResult;
import org.frias.avalon.promociones.entities.Promotion;
import org.frias.avalon.promociones.factory.oters.interfaz.PromotionStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("PROMO_GLOBAL")
public class PromoGlobal implements PromotionStrategy {

    @Override
    public DiscountTempResult applyDiscount(BigDecimal basePrice, Promotion promo) {

        BigDecimal discount = basePrice.multiply(promo.getDiscount()) // 100 * 15.5 = 1550
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP); // 1550 / 100 = 15.50

        return new DiscountTempResult(
                promo.getDiscount(),
                promo.getDescription(),
                basePrice.subtract(discount).setScale(2, RoundingMode.HALF_UP)
        );
    }

    @Override
    public String getPromotionType() {
        return "PROMO_GLOBAL";
    }
}
