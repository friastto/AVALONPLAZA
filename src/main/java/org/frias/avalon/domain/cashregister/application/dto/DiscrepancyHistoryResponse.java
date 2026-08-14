package org.frias.avalon.domain.cashregister.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DiscrepancyHistoryResponse(
        Long sessionId,
        Long outletId,
        Long employeeId,
        String employeeName,
        String employeeNumberId,
        Long employeeStatusId,
        BigDecimal initialBase,
        BigDecimal expectedCash,
        BigDecimal actualCash,
        BigDecimal difference,
        String discrepancyType,
        String notes,
        String status,
        LocalDateTime openedAt,
        LocalDateTime closedAt
) {}
