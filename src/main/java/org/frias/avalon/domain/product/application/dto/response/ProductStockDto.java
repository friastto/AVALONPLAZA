package org.frias.avalon.domain.product.application.dto.response;

public record ProductStockDto(
        String productName,
        Integer stock
) {
}
