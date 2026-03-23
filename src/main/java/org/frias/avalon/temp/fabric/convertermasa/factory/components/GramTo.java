package org.frias.avalon.temp.fabric.convertermasa.factory.components;

import org.frias.avalon.temp.fabric.convertermasa.factory.strategy.ConvertStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class GramTo implements ConvertStrategy {


    @Override
    public BigDecimal redyConvertEntry(BigDecimal quantity, Boolean exit) {

        if (quantity == null) return BigDecimal.ZERO;

        return quantity.setScale(2, RoundingMode.HALF_UP);

    }



}
