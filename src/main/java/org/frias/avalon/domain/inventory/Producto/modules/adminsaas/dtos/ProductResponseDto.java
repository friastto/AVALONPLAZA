package org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos;

import java.math.BigDecimal;


public record ProductResponseDto(

        Long id,

        String barcode,

        String name,

        String description,

        BigDecimal price,

        BigDecimal discount,

        BigDecimal finalPrice,

        String category,

        String unitMeasure,

        String stock,

        String imageUrl
){
}
