package org.frias.avalon.domain.cashregister.presentation.dto;

public record CashierHistorySummaryResponse(
        Long userId,
        Long personId,
        String fullName,
        String numberId,
        Long userStatusId,
        Long sessionsCount
) {}
