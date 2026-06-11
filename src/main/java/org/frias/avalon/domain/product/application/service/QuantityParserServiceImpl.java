package org.frias.avalon.domain.product.application.service;

import org.frias.avalon.core.exeptions.DomainValidationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class QuantityParserServiceImpl implements QuantityParserService {

    @Override
    public BigDecimal parseAndValidate(String quantity) {
        if (quantity == null || quantity.trim().isBlank()) {
            throw new DomainValidationException("Quantity string cannot be null or empty.");
        }

        BigDecimal validQuantity;
        try {
            // Normalize decimal separator (replace comma with dot)
            String normalizedQuantity = quantity.trim().replace(",", ".");
            validQuantity = new BigDecimal(normalizedQuantity);
            
        } catch (NumberFormatException e) {
            throw new DomainValidationException(
                "Invalid number format for quantity: '" + quantity + "'. Expected a valid number (e.g., '1.5' or '1,5')."
            );
        }

        if (validQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("Quantity cannot be negative: " + validQuantity);
        }

        return validQuantity;
    }
}
