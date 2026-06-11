package org.frias.avalon.domain.product.application.dto;

public record BarcodeDto(Long id, String barcode, Long productOutlet, String description) {
}
