package org.frias.avalon.fabric.convertermasa.factory.strategy;

import java.math.BigDecimal;

public interface ConvertStrategy {

    BigDecimal redyConvertEntry(BigDecimal quantity, Boolean exit);
}
