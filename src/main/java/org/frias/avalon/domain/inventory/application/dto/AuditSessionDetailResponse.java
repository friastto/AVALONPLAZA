package org.frias.avalon.domain.inventory.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record AuditSessionDetailResponse(
        Long id,
        Long outletId,
        Long companyId,
        String status,
        Long openedByUserId,
        String openedByName,
        Long closedByUserId,
        String closedByName,
        String notes,
        BigDecimal totalSoftwareValue,
        BigDecimal totalPhysicalValue,
        BigDecimal totalDifferenceValue,
        String openedAt,
        String closedAt,
        int totalItems,
        int countedItems,
        int discrepancyItems,
        List<AuditItemDto> items,
        List<AuditSignatureDto> signatures
) {}
