package org.frias.avalon.domain.inventory.application.dto;

import java.math.BigDecimal;

public record AuditItemDto(
        Long id,
        Long auditSessionId,
        Long productOutletId,
        String productName,
        String barcode,
        String unitMeasure,
        BigDecimal unitPrice,
        BigDecimal softwareStock,
        BigDecimal softwareValue,
        BigDecimal physicalStock,
        BigDecimal physicalValue,
        BigDecimal differenceStock,
        BigDecimal differenceValue,
        String discrepancyReason,
        Long countedByUserId,
        String countedByName,
        String status,
        String updatedAt
) {}
