package org.frias.avalon.Producto.modules.adminsaas.dtos;

public record ProductBarcodeRequestDto(
       Long productId,
       String newBarcode,
       String label
) {
}
