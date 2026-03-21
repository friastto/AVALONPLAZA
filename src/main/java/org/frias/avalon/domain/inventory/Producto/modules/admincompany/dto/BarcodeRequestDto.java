package org.frias.avalon.domain.inventory.Producto.modules.admincompany.dto;

public record BarcodeRequestDto(

    Long productId,
    String newBarcode,
    String desc

) {
}
