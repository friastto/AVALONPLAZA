package org.frias.avalon.domain.cashregister.application.dto;

import java.math.BigDecimal;

public record ConsolidatedHistoryResponse(
        String date,
        Long outletId,
        BigDecimal totalConsolidatedAmount,
        BigDecimal totalCashSales,
        BigDecimal totalDigitalSales,
        BigDecimal totalCardSales,
        BigDecimal totalCreditSales,
        BigDecimal totalExpenses,
        BigDecimal totalPickups,
        int activeSessionsCount,
        int closedSessionsCount
) {}
