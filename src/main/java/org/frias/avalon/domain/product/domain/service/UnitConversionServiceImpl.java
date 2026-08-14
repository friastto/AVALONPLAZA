package org.frias.avalon.domain.product.domain.service;

import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Pure Java Domain service implementation for unit conversions.
 * Free of Spring Framework / Lombok annotations.
 */
public class UnitConversionServiceImpl implements UnitConversionService {

    private final MasterTreeProvider masterTreeProvider;

    public UnitConversionServiceImpl(MasterTreeProvider masterTreeProvider) {
        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    public Integer convertToSmallestUnit(BigDecimal quantity, String unit) {
        if (quantity == null) {
            return 0;
        }
        if (unit == null || unit.isBlank()) {
            throw new DomainValidationException("Unit of measurement cannot be null or blank");
        }

        String normalizedUnit = unit.trim().toUpperCase();

        return switch (normalizedUnit) {
            case "KG" -> quantity.multiply(BigDecimal.valueOf(1000)).intValue();
            case "LB" -> quantity.multiply(BigDecimal.valueOf(453.592)).intValue();
            case "G", "GR" -> quantity.intValue();
            case "@", "ARROBA" -> quantity.multiply(BigDecimal.valueOf(11339)).intValue();
            case "TON" -> quantity.multiply(BigDecimal.valueOf(1_000_000)).intValue();
            case "OZ" -> quantity.multiply(BigDecimal.valueOf(28.3495)).intValue();
            case "UND", "UNIDAD" -> quantity.intValue();
            case "L", "LT" -> quantity.multiply(BigDecimal.valueOf(1000)).intValue();
            case "ML" -> quantity.intValue();
            default -> throw new DomainValidationException("Unrecognized unit for conversion: " + normalizedUnit);
        };
    }

    @Override
    public String convertFromSmallestUnit(Integer baseQuantity, Long unitId) {
        if (baseQuantity == null) {
            return "0";
        }
        if (unitId == null) {
            throw new DomainValidationException("Unit ID is required for conversion");
        }

        MasterRoot unitNode = masterTreeProvider.getTree().getByIdOrThrow(unitId);
        String unitCode = unitNode.getShortName().toUpperCase();

        BigDecimal baseValue = new BigDecimal(baseQuantity);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#.##", symbols);

        return switch (unitCode) {
            case "G", "GR" -> baseQuantity + " GR";
            case "KG" -> df.format(baseValue.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP)) + " KG";
            case "LB" -> df.format(baseValue.divide(BigDecimal.valueOf(453.592), 2, RoundingMode.HALF_UP)) + " LB";
            case "@", "ARROBA" -> df.format(baseValue.divide(BigDecimal.valueOf(11339), 2, RoundingMode.HALF_UP)) + " ARROBA";
            case "TON" -> df.format(baseValue.divide(BigDecimal.valueOf(1_000_000), 4, RoundingMode.HALF_UP)) + " TON";
            case "OZ" -> df.format(baseValue.divide(BigDecimal.valueOf(28.3495), 2, RoundingMode.HALF_UP)) + " OZ";
            case "UND", "UNIDAD" -> baseQuantity + " UND";
            case "ML" -> baseQuantity + " ML";
            case "L", "LT" -> df.format(baseValue.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP)) + " L";
            default -> baseQuantity.toString() + " " + unitCode;
        };
    }
}
