package org.frias.avalon.domain.inventory.application.event;

/**
 * Domain event fired when a store manager performs a manual stock adjustment.
 * Consumed asynchronously by StockAdjustmentEventListener to send an
 * anti-fraud audit notification email to the Company Manager.
 */
public record StockAdjustmentNotificationEvent(
        Long outletId,
        Long productOutletId,
        String productName,
        Integer quantityBefore,
        Integer quantityAfter,
        String reason,
        Long operatorId,
        String operatorName
) {
}
