package org.frias.avalon.fabric.priceCalculator;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PriceCalculatorImple implements PriceCalculator {

    @Override
    public  BigDecimal calcularTotalPorPeso(
            BigDecimal precioBase,
            String unidadMedida,
            BigDecimal gramosVendidos
    ) {
        BigDecimal precioPorGramo = calcularPrecioPorGramo(precioBase, unidadMedida);

        return precioPorGramo.multiply(gramosVendidos)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal obtenerFactorEnGramos(String unidadMedida) {
        switch (unidadMedida.toUpperCase()) {
            case "LB":
                return new BigDecimal("453.59237");
            case "KG":
                return new BigDecimal("1000");
            case "GR":
                return BigDecimal.ONE;
            default:
                throw new IllegalArgumentException("Unidad no soportada: " + unidadMedida);
        }
    }


    @Override
    public BigDecimal calcularPrecioPorGramo(BigDecimal precioBase, String unidadMedida) {

        BigDecimal factor = obtenerFactorEnGramos(unidadMedida);

        return precioBase.divide(
                factor,
                10,
                RoundingMode.HALF_UP
        );
    }




}
