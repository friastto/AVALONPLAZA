package org.frias.avalon.domain.sale.infrastructure.service;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.sale.domain.service.SaleWeightConversionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

@Service
public class SaleWeightConversionServiceImpl implements SaleWeightConversionService {

    private static final BigDecimal GRAMS_PER_POUND = new BigDecimal("453.59237");
    private static final BigDecimal GRAMS_PER_KILO = new BigDecimal("1000");
    private static final BigDecimal MILLILITERS_PER_LITER = new BigDecimal("1000");

    private static final Set<String> WEIGHABLE_UNITS = Set.of("KG", "G", "LB", "L", "ML");

    @Override
    public Integer convertToBaseUnit(BigDecimal quantity, String unitShortName) {
        if (quantity == null) {
            throw new BusinessException("La cantidad no puede ser nula");
        }
        if (unitShortName == null) {
            throw new BusinessException("La unidad de medida es requerida");
        }

        String unit = unitShortName.trim().toUpperCase();

        BigDecimal baseValue;
        switch (unit) {
            case "LB":
                baseValue = quantity.multiply(GRAMS_PER_POUND);
                break;
            case "KG":
                baseValue = quantity.multiply(GRAMS_PER_KILO);
                break;
            case "G":
            case "ML":
                baseValue = quantity;
                break;
            case "L":
                baseValue = quantity.multiply(MILLILITERS_PER_LITER);
                break;
            default:
                throw new BusinessException("Unidad de medida '" + unitShortName + "' no soportada para conversión de peso/volumen.");
        }

        return baseValue.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    @Override
    public String formatFromBaseUnit(Integer baseQuantity, String unitShortName) {
        if (baseQuantity == null) {
            return "0";
        }
        if (unitShortName == null) {
            return baseQuantity.toString();
        }

        String unit = unitShortName.trim().toUpperCase();
        BigDecimal base = new BigDecimal(baseQuantity);
        BigDecimal displayValue;

        switch (unit) {
            case "LB":
                displayValue = base.divide(GRAMS_PER_POUND, 2, RoundingMode.HALF_UP);
                break;
            case "KG":
                displayValue = base.divide(GRAMS_PER_KILO, 2, RoundingMode.HALF_UP);
                break;
            case "G":
            case "ML":
                displayValue = base.setScale(2, RoundingMode.HALF_UP);
                break;
            case "L":
                displayValue = base.divide(MILLILITERS_PER_LITER, 2, RoundingMode.HALF_UP);
                break;
            default:
                return baseQuantity + " " + unitShortName;
        }

        // Remover decimales innecesarios, p.ej. "1.00" -> "1"
        String formatted = displayValue.stripTrailingZeros().toPlainString();
        return formatted + " " + unitShortName;
    }

    @Override
    public boolean isWeighable(String unitShortName) {
        if (unitShortName == null) return false;
        return WEIGHABLE_UNITS.contains(unitShortName.trim().toUpperCase());
    }
}
