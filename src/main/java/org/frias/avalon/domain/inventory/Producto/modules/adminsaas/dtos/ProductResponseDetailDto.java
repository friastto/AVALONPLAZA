package org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos;

import java.math.BigDecimal;


public record ProductResponseDetailDto(

        Long id,

        String codeBar,

        String name,

        String description,

        String medida,

        BigDecimal price

){
}
