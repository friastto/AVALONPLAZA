package org.frias.avalon.domain.cashregister.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Immutable DTO record for Cash Expense response.
 */
public record CashExpenseResponse(
        Long id,
        Long cashSessionId,
        BigDecimal amount,
        String reason,
        Long registeredBy,
        LocalDateTime createdAt
) {}
