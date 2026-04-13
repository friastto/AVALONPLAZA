package org.frias.avalon.domain.product.application.dto;

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
