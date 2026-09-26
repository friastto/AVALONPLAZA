package org.frias.avalon.domain.product.application.dto.response;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para productos del Catalogo Global Maestro de Avalon (Nivel 1).
 */
public record GlobalProductResponseDto(
    Long id,
    String name,
    String description,
    String barcode,
    MasterRefDto category,
    MasterRefDto unitMeasure,
    String imageUrl,
    MasterRefDto status,
    LocalDateTime createdAt
) {}
