package org.frias.avalon.Producto.dtos;

import java.math.BigDecimal;


public record ProductResponseDto(

        Long id,

        String codigoBarras,

        String nombre,

        String descripcion,

        BigDecimal precio,

        BigDecimal descuento,

        BigDecimal precioFinal,

        String categoria,

        String medida,

        String stock
){
}
