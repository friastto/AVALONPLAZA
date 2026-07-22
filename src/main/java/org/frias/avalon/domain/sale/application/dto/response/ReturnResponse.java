package org.frias.avalon.domain.sale.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ReturnResponse(
        Long id,
        UUID returnCode,
        UUID originalSaleCode,
        Long originalSaleId,
        BigDecimal totalRefundAmount,
        String reason,
        String resolutionType,
        String status,
        String clientFullName,
        String clientNumberid,
        Long outletId,
        Long employeeId,
        LocalDateTime returnDate,
        List<ReturnItemResponse> items
) {}
