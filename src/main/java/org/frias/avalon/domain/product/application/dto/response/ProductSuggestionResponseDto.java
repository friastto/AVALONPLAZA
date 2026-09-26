package org.frias.avalon.domain.product.application.dto.response;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para visualizacion de sugerencias y productos corporativos enriquecidos con MasterTree.
 */
public record ProductSuggestionResponseDto(
    Long id,
    Long productId,
    String productName,
    String description,
    String barcode,
    MasterRefDto category,
    MasterRefDto unitMeasure,
    BigDecimal suggestedPrice,
    String imageUrl,
    Long companyId,
    String companyName,
    Long outletId,
    String outletName,
    MasterRefDto status,
    LocalDateTime createdAt
) {}
