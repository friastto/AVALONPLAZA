package org.frias.avalon.domain.product.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO para registrar una solicitud de creacion o adopcion de producto desde tienda (Nivel 3)
 * hacia la gerencia de la empresa (Nivel 2).
 */
public record ProductSuggestionRequestDto(
    Long productId,
    Long companyId,
    Long outletId,
    String name,
    String description,
    String barcode,
    Long categoryId,
    Long unitMeasureId,
    @NotNull(message = "El precio sugerido es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio sugerido debe ser mayor a cero")
    BigDecimal suggestedPrice,
    String imageUrl
) {}
