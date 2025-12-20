package org.frias.avalon.Producto.dtos;

import java.math.BigDecimal;


public record ProductResponseDetailDto(

        Long id,

        String codeBar,

        String name,

        String description,

        String medida

){
}
