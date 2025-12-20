package org.frias.avalon.Producto.factory.components;

import org.frias.avalon.Producto.factory.strategy.DiscountStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("EMPLOYEE_DAY")
public class DiscountEmployee implements DiscountStrategy {
    // Es buena práctica definir el porcentaje como una constante
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.20");

    @Override
    public BigDecimal applyDiscount(BigDecimal total) {

        if (total == null) return BigDecimal.ZERO;

        return total.multiply(DISCOUNT_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
