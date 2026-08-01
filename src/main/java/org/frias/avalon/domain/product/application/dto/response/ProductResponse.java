package org.frias.avalon.domain.product.application.dto.response;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for product responses.
 * Stock is presented as a human-readable string.
 * effectiveImageUrl resolves the best available image across 3 tiers:
 *   localImageUrl (store L3) > customImageUrl (company L2) > imageUrl (Avalon L1)
 */
public record ProductResponse(
    Long id,
    String name,
    String description,
    String displayStock, // Campo para mostrar el stock formateado (ej. "1.5 KG")
    String imageUrl,
    String effectiveImageUrl, // Imagen resuelta con prioridad L3 > L2 > L1
    BigDecimal price,
    Long outletId,
    MasterDataResponseDto status,
    String barCode,
    Long unitMeasureId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

