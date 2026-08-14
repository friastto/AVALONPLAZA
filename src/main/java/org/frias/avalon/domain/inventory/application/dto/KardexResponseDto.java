package org.frias.avalon.domain.inventory.application.dto;

import java.time.LocalDateTime;

/**
 * Immutable Kardex response DTO.
 */
public record KardexResponseDto(
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
