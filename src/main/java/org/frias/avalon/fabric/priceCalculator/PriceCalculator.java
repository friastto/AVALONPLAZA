package org.frias.avalon.fabric.priceCalculator;

import java.math.BigDecimal;


public interface PriceCalculator {

    BigDecimal calcularTotalPorPeso(BigDecimal precioBase, String unidadMedida, BigDecimal gramosVendidos);

    BigDecimal obtenerFactorEnGramos(String unidadMedida);

    BigDecimal calcularPrecioPorGramo(BigDecimal precioBase, String unidadMedida);
}
