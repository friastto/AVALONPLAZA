package org.frias.avalon.domain.product.application.dto.response;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for product responses.
 * Stock is presented as a human-readable string.
 * displayReservedGlobal and displayReservedUser reflect active order reserves.
 */
public record ProductResponse(
    Long id,
    String name,
    String description,
    String displayStock, // Campo para mostrar el stock formateado (ej. "1.5 KG")
    String displayReservedGlobal, // Total reservado en tienda (ej. "2.0 UND" o "1.5 KG")
    String displayReservedUser,   // Total reservado por el usuario (ej. "1.0 UND" o "0.5 KG")
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
