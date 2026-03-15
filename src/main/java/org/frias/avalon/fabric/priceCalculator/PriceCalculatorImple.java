package org.frias.avalon.fabric.priceCalculator;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PriceCalculatorImple implements PriceCalculator {

    @Override
    public  BigDecimal calculatePriceXWeight(
            BigDecimal basePrice,
            String unit,
            BigDecimal requiredGram
    ) {
        BigDecimal priceGram = calculatePriceGram(basePrice, unit);

        return priceGram.multiply(requiredGram)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getFactorGram(String unit) {
        switch (unit.toUpperCase()) {
            case "LB":
                return new BigDecimal("453.59237");
            case "KG":
                return new BigDecimal("1000");
            case "GR":
                return BigDecimal.ONE;
            default:
                throw new IllegalArgumentException("Unidad no soportada: " + unit);
        }
    }


    @Override
    public BigDecimal calculatePriceGram(BigDecimal basePrice, String unit) {

        BigDecimal factor = getFactorGram(unit);

        return basePrice.divide(
                factor,
                10,
                RoundingMode.HALF_UP
        );
    }




}
