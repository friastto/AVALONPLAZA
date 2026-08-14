package org.frias.avalon.domain.cashregister.application.dto;

public record CashierHistorySummaryResponse(
        Long userId,
        Long personId,
        String fullName,
        String numberId,
        Long userStatusId,
        Long sessionsCount
) {}
