package org.frias.avalon.domain.product.application.dto.saas;

import jakarta.validation.constraints.NotBlank;

public record ProductAvalonResponseDto(

        Long id,

        String name,

        String description,

        String categoryId,

        String unitMeasureId,

        String imageUrl
) { }
