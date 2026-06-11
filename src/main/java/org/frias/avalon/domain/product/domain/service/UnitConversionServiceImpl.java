package org.frias.avalon.domain.product.domain.service;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UnitConversionServiceImpl implements UnitConversionService {

    private final MasterTreeProvider masterTreeProvider;

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
            // Unidades de Masa (Base: Gramos)
            case "KG" -> quantity.multiply(BigDecimal.valueOf(1000)).intValue();
            case "LB" -> quantity.multiply(BigDecimal.valueOf(453.592)).intValue();
            case "G", "GR" -> quantity.intValue();
            case "@", "ARROBA" -> quantity.multiply(BigDecimal.valueOf(11339)).intValue();
            case "TON" -> quantity.multiply(BigDecimal.valueOf(1_000_000)).intValue();
            case "OZ" -> quantity.multiply(BigDecimal.valueOf(28.3495)).intValue();

            // Unidades Discretas
            case "UND", "UNIDAD" -> quantity.intValue();

            // Unidades de Volumen (Base: Mililitros)
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
        
        // Usamos Locale.US para asegurar que el separador decimal sea un punto
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        // Formato para mostrar hasta 2 decimales si los hay, pero sin obligar a mostrar ".00"
        DecimalFormat df = new DecimalFormat("#.##", symbols);

        return switch (unitCode) {
            // Unidades de Masa (convertidas desde gramos)
            case "G", "GR" -> baseQuantity + " GR";
            case "KG" -> df.format(baseValue.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP)) + " KG";
            case "LB" -> df.format(baseValue.divide(BigDecimal.valueOf(453.592), 2, RoundingMode.HALF_UP)) + " LB";
            case "@", "ARROBA" -> df.format(baseValue.divide(BigDecimal.valueOf(11339), 2, RoundingMode.HALF_UP)) + " ARROBA";
            case "TON" -> df.format(baseValue.divide(BigDecimal.valueOf(1_000_000), 4, RoundingMode.HALF_UP)) + " TON";
            case "OZ" -> df.format(baseValue.divide(BigDecimal.valueOf(28.3495), 2, RoundingMode.HALF_UP)) + " OZ";
            
            // Unidades discretas
            case "UND", "UNIDAD" -> baseQuantity + " UND";

            // Unidades de Volumen (convertidas desde mililitros)
            case "ML" -> baseQuantity + " ML";
            case "L", "LT" -> df.format(baseValue.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP)) + " L";

            default -> baseQuantity.toString() + " " + unitCode; // Si no hay regla, devolver el valor base con su código
        };
    }
}
