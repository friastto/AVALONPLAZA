package org.frias.avalon.Producto.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record ProductRequestCreate(

        @NotBlank(message = "producto sin codigo de barras")
        @NotBlank(message = "producto sin codigo de barras")
        String codeBar,

        @NotBlank(message="producto sin nombre")
        @NotNull(message = "producto sin nombre")
        String name,

        @NotBlank(message = "producto sin descripcion")
        @NotNull(message = "producto sin descripcion")
         String desc,

        @NotBlank(message = "producto sin precio")
        @NotNull(message = "producto sin precio")
        BigDecimal price,

        @NotBlank(message = "producto sin categoria")
        @NotNull(message = "producto sin categoria")
         Long categoryId,

        @NotBlank(message = "producto sin unidad de medida")
        @NotNull(message = "producto sin unidad de medida")
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
