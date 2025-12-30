package org.frias.avalon.fabric.convertermasa.factory.components;

import org.frias.avalon.fabric.convertermasa.factory.strategy.ConvertStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("KG")
public class KgAndGram implements ConvertStrategy {

    private static final BigDecimal GRAMOS_POR_KILO = new BigDecimal("1000");



    @Override
    public BigDecimal redyConvertEntry(BigDecimal quantity, Boolean exit) {

        if (quantity == null) return BigDecimal.ZERO;


        return exit ?
                quantity.divide(GRAMOS_POR_KILO).setScale(2, RoundingMode.HALF_UP)
                : quantity.multiply(GRAMOS_POR_KILO).setScale(6, RoundingMode.HALF_UP);

    }


}
