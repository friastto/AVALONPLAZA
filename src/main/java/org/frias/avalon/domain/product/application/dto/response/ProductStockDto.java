package org.frias.avalon.domain.product.application.dto.response;

public record ProductStockDto(
        String productName,
        Integer stock,
        String displayStock,
        String unitMeasure
) {
    public ProductStockDto(String productName, Integer stock) {
        this(productName, stock, stock != null ? stock.toString() : "0", null);
    }
}
