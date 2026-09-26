package org.frias.avalon.domain.product.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO para la adopcion corporativa de un producto de Nivel 1 (Avalon Global)
 * hacia Nivel 2 (Empresa) por parte del Gerente de Empresa (GERGEN).
 */
public record AdoptGlobalProductRequestDto(
    @NotNull(message = "El ID del producto global es obligatorio")
    Long productId,

    Long companyId,

    @NotNull(message = "El precio corporativo es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio corporativo debe ser mayor a cero")
    BigDecimal customPrice,

    String customImageUrl
) {}
