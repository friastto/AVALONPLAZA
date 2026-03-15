package org.frias.avalon.Producto.modules.admincompany.services.dto;

public record BarcodeRequestDto(

    Long productId,
    String newBarcode,
    String desc

) {
}
