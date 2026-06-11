package org.frias.avalon.domain.product.application.dto.response;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for product responses.
 * Stock is presented as a human-readable string.
 */
public record ProductResponse(
    Long id,
    String name,
    String description,
    String displayStock, // Campo para mostrar el stock formateado (ej. "1.5 KG")
    String imageUrl,
    BigDecimal price,
    Long outletId,
    MasterDataResponseDto status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
