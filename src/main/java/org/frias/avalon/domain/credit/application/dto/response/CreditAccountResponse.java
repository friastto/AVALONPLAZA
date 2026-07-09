package org.frias.avalon.domain.credit.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditAccountResponse(
        Long id,
        Long clientId,
        String clientName,
        String clientNumberid,
        Long outletId,
        BigDecimal creditLimit,
        BigDecimal currentDebt,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
