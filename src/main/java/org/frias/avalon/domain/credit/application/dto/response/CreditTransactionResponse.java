package org.frias.avalon.domain.credit.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditTransactionResponse(
        Long id,
        Long creditAccountId,
        Long saleId,
        String type, // PURCHASE, PAYMENT
        BigDecimal amount,
        BigDecimal previousDebt,
        BigDecimal newDebt,
        String notes,
        Long registeredBy,
        String registeredByName,
        LocalDateTime createdAt
) {}
