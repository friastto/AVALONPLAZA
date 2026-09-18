package org.frias.avalon.domain.inventory.application.dto;

import java.math.BigDecimal;

/**
 * Request DTO for stock adjustment operations.
 * Used when a store manager corrects physical inventory
 * (e.g., shelf count shows 5 panelas but system says 3).
 */
public record StockAdjustmentRequest(
        Long productOutletId,
        Long outletId,
        Integer newQuantity,
        BigDecimal decimalQuantity,
        String reason,
        Long operatorId
) {
    public StockAdjustmentRequest(Long productOutletId, Long outletId, Integer newQuantity, String reason, Long operatorId) {
        this(productOutletId, outletId, newQuantity, null, reason, operatorId);
    }
}
