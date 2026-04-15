package org.frias.avalon.domain.product.application.dto.saas;

import jakarta.validation.constraints.NotBlank;

public record ProductAvalonRequestDataDto(

        @NotBlank(message = "producto sin name")
        String name,

        @NotBlank(message = "producto sin description")
        String description,

        @NotBlank(message = "producto sin categoria")
        Long categoryId,

        @NotBlank(message = "producto sin unidad de medida")
        Long unitMeasureId

) { }
