package org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos;

public record ProductBarcodeRequestDto(
       Long productId,
       String newBarcode,
       String label
) {
}
