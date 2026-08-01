package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.domain.inventory.application.dto.StockAdjustmentRequest;
import org.frias.avalon.domain.inventory.application.event.StockAdjustmentNotificationEvent;
import org.frias.avalon.domain.inventory.infrastructure.entity.StockMovementEntity;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaStockMovementRepository;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

/**
 * Use case for manual stock adjustments performed by Store Managers.
 *
 * Business rules:
 * 1. The store manager can adjust stock immediately without waiting for company approval.
 *    This prevents POS sales from being blocked.
 * 2. Every adjustment generates an immutable Kardex (StockMovementEntity) record.
 * 3. A StockAdjustmentNotificationEvent is published asynchronously so the Company
 *    Manager receives an anti-fraud audit alert email.
 *
 * Example: shelf audit shows 5 panelas but system says 3.
 *   -> stock updated 3 -> 5 immediately
 *   -> Kardex entry: ADJUSTMENT_SURPLUS, delta=+2, reason="Conteo fisico de estanteria"
 *   -> Async email to gerente@empresa.com
 */
@Service
public class StockAdjustmentUseCaseImpl {

    private final JpaProductOutletRepository productOutletRepository;
    private final JpaStockMovementRepository stockMovementRepository;
    private final ApplicationEventPublisher eventPublisher;

    public StockAdjustmentUseCaseImpl(
            JpaProductOutletRepository productOutletRepository,
            JpaStockMovementRepository stockMovementRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.productOutletRepository = productOutletRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void execute(StockAdjustmentRequest request) {

        // 1. Load the store product record
        ProductOutlet productOutlet = productOutletRepository.findById(request.productOutletId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "ProductOutlet not found for id: " + request.productOutletId()));

        int before = productOutlet.getStock();
        int after = request.newQuantity();
        int delta = after - before;

        // 2. Determine movement type based on delta direction
        String movementType = delta >= 0 ? "ADJUSTMENT_SURPLUS" : "MERMA";

        // 3. Update stock immediately (store manager autonomy — never blocks POS)
        productOutlet.setStock(after);
        productOutletRepository.save(productOutlet);

        // 4. Write immutable Kardex entry
        StockMovementEntity movement = StockMovementEntity.builder()
                .productOutletId(request.productOutletId())
                .outletId(request.outletId())
                .movementType(movementType)
                .quantityBefore(before)
                .quantityAfter(after)
                .quantityDelta(delta)
                .reason(request.reason())
                .operatorId(request.operatorId())
                .build();

        stockMovementRepository.save(movement);

        // 5. Publish async anti-fraud notification to Company Manager
        eventPublisher.publishEvent(new StockAdjustmentNotificationEvent(
                request.outletId(),
                request.productOutletId(),
                productOutlet.getLocalName() != null ? productOutlet.getLocalName() : "Producto ID " + productOutlet.getId(),
                before,
                after,
                request.reason(),
                request.operatorId(),
                "Operador ID " + request.operatorId()
        ));
    }
}
