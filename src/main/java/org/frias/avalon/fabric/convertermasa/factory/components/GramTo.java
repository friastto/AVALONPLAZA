package org.frias.avalon.fabric.convertermasa.factory.components;

import org.frias.avalon.fabric.convertermasa.factory.strategy.ConvertStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class GramTo implements ConvertStrategy {


    @Override
    public BigDecimal redyConvertEntry(BigDecimal quantity, Boolean exit) {

        if (quantity == null) return BigDecimal.ZERO;

        return quantity.setScale(2, RoundingMode.HALF_UP);

    }



}
