package org.frias.avalon.temp.inventory.Producto.modules.adminsaas.dtos;

public record ProductBarcodeRequestDto(
       Long productId,
       String newBarcode,
       String label
) {
}
