package org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos;

import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record ProductCompanyRequestCreate(

        String codeBar,

        Long productId,

        String name,

        String desc,

        BigDecimal price,

        Long categoryId,

        Long unitId,

        @Pattern(
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
