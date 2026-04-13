package org.frias.avalon.domain.product.application.dto;

public record ProductBarcodeRequestDto(
       Long productId,
       String newBarcode,
       String label
) {
}
