package org.frias.avalon.domain.product.application.dto.saas;


public record ProductAvalonResponseDto(

        Long id,

        String name,

        String description,

        String categoryId,

        String unitMeasureId,

        String status,

        String imageUrl
) { }
