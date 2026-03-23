package org.frias.avalon.temp.inventory.Producto.modules.admincompany.dto;

public record BarcodeRequestDto(

    Long productId,
    String newBarcode,
    String desc

) {
}
