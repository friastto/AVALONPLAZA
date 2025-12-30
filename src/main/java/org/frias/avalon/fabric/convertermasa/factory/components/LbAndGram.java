package org.frias.avalon.fabric.convertermasa.factory.components;

import org.frias.avalon.fabric.convertermasa.factory.strategy.ConvertStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("LB")
public class LbAndGram implements ConvertStrategy {

    private static final BigDecimal GRAMOS_POR_LIBRA = new BigDecimal("453.59237");



    @Override
    public BigDecimal redyConvertEntry(BigDecimal quantity, Boolean exit) {

        if (quantity == null) return BigDecimal.ZERO;


        return exit ?
                    quantity.divide(GRAMOS_POR_LIBRA, 2, RoundingMode.HALF_UP)

                : quantity.multiply(GRAMOS_POR_LIBRA).setScale(6, RoundingMode.HALF_UP);

    }


}
