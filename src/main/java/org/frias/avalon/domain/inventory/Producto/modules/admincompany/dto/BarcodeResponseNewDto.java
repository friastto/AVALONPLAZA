package org.frias.avalon.domain.inventory.Producto.modules.admincompany.dto;

public record BarcodeResponseNewDto(
        Long id,
        Long productId,
        String newBarcode,
        String desc


) {
}
