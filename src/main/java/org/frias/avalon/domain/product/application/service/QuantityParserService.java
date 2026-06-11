package org.frias.avalon.domain.product.application.service;

import java.math.BigDecimal;

/**
 * Application service responsible for parsing and validating string quantities.
 */
public interface QuantityParserService {

    /**
     * Parses a string quantity, normalizes it, and validates its format.
     *
     * @param quantity The string representation of the quantity (e.g., "1,5" or "1.5").
     * @return A valid, non-negative BigDecimal.
     * @throws org.frias.avalon.core.exeptions.DomainValidationException if the format is invalid or the number is negative.
     */
    BigDecimal parseAndValidate(String quantity);
}
