package org.frias.avalon.temp.fabric.convertermasa.factory;

import java.math.BigDecimal;

public interface ConvertFactoryService {

    BigDecimal convertTo(String quantity, String unitToConvert, Boolean exit);
}
