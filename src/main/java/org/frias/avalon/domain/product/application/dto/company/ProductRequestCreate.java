package org.frias.avalon.domain.product.application.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record ProductRequestCreate(

        @NotBlank(message = "producto sin codigo de barras")
        String codeBar,

        @NotBlank(message="producto sin name")
        String name,

        @NotBlank(message = "producto sin description")
        String desc,

        @NotBlank(message = "producto sin price")
        BigDecimal price,

        @NotBlank(message = "producto sin category")
        Long categoryId,

        @NotBlank(message = "producto sin unidad de unitMeasure")
         Long unitId,

        @jakarta.validation.constraints.Pattern(
                regexp = "^[0-9]+([.,][0-9]+)?$",
                message = "el Stock debe contener solo números y un punto o coma como decimal"
        )
        String stock,

        @Pattern(
                regexp = "^[0-9]+([.,][0-9]+)?$",
                message = "La cantidad debe contener solo números y un punto o coma como decimal"
        )
        String cant

){}
