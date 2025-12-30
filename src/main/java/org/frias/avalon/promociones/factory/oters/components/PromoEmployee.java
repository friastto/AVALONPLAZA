package org.frias.avalon.promociones.factory.oters.components;

import org.frias.avalon.promociones.dtos.DiscountTempResult;
import org.frias.avalon.promociones.entities.Promotion;
import org.frias.avalon.promociones.factory.oters.interfaz.PromotionStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("PROMO_EMPLOYEE")
public class PromoEmployee implements PromotionStrategy {
    // Regla de oro del negocio: 30% fijo
    private static final BigDecimal FIXED_RATE = new BigDecimal("0.30");


    @Override
    public DiscountTempResult applyDiscount(BigDecimal basePrice, Promotion promo) {
        BigDecimal discount = basePrice.multiply(FIXED_RATE);

        return new DiscountTempResult(
                promo.getDiscount(),
                "PROMO_EMPLEADO",
                basePrice.subtract(discount).setScale(2, RoundingMode.HALF_UP)

        );
    }

    @Override
    public String getPromotionType() {
        return "PROMO_EMPLOYEE";
    }
}
