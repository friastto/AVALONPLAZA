package org.frias.avalon.domain.inventory.application.dto;

import java.time.LocalDateTime;

/**
 * Immutable DTO record for stock adjustment response.
 */
public record StockAdjustmentResponse(
        Long id,
        Long productOutletId,
        Long outletId,
        String movementType,
        Integer quantityBefore,
        Integer quantityAfter,
        Integer quantityDelta,
        String reason,
        Long operatorId,
        LocalDateTime createdAt
) {}
