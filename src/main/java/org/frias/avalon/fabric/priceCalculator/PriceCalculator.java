package org.frias.avalon.fabric.priceCalculator;

import java.math.BigDecimal;


public interface PriceCalculator {

    BigDecimal calculatePriceXWeight(BigDecimal precioBase, String unidadMedida, BigDecimal gramosVendidos);

    BigDecimal getFactorGram(String unidadMedida);

    BigDecimal calculatePriceGram(BigDecimal precioBase, String unidadMedida);
}
