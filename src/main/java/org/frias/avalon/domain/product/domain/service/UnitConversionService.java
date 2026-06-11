package org.frias.avalon.domain.product.domain.service;

import java.math.BigDecimal;

/**
 * Domain service responsible for converting quantities between different units.
 */
public interface UnitConversionService {

    /**
     * Converts a given quantity and unit to the smallest base unit as an integer.
     * For example, converts 1.5 KG to 1500.
     *
     * @param quantity The numerical value of the quantity.
     * @param unit The string representing the unit (e.g., "KG", "UND").
     * @return The quantity in the smallest base unit (e.g., grams).
     */
    Integer convertToSmallestUnit(BigDecimal quantity, String unit);

    /**
     * Converts a quantity from its smallest base unit to a human-readable string
     * in a larger, more convenient unit (e.g., grams to KG).
     *
     * @param baseQuantity The quantity in its smallest unit (e.g., grams).
     * @param unitId The ID of the unit of measurement from MasterData.
     * @return A formatted string representing the quantity in a convenient unit (e.g., "1.5 KG").
     */
    String convertFromSmallestUnit(Integer baseQuantity, Long unitId);
}
