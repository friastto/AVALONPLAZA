package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.inventory.application.dto.StockAdjustmentRequest;
import org.frias.avalon.domain.inventory.application.dto.StockAdjustmentResponse;
import org.frias.avalon.domain.inventory.application.event.StockAdjustmentNotificationEvent;
import org.frias.avalon.domain.inventory.infrastructure.entity.StockMovementEntity;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaStockMovementRepository;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of StockAdjustmentUseCase Input Port.
 */
@Service
public class StockAdjustmentUseCaseImpl implements StockAdjustmentUseCase {

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
    @Override
    public StockAdjustmentResponse execute(StockAdjustmentRequest request) {

        ProductOutlet productOutlet = productOutletRepository.findById(request.productOutletId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ProductOutlet not found for id: " + request.productOutletId()));

        int before = productOutlet.getStock();
        int after = request.newQuantity();
        int delta = after - before;

        String movementType = delta >= 0 ? "ADJUSTMENT_SURPLUS" : "MERMA";

        productOutlet.setStock(after);
        productOutletRepository.save(productOutlet);

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

        StockMovementEntity saved = stockMovementRepository.save(movement);

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

        return new StockAdjustmentResponse(
                saved.getId(),
                saved.getProductOutletId(),
                saved.getOutletId(),
                saved.getMovementType(),
                saved.getQuantityBefore(),
                saved.getQuantityAfter(),
                saved.getQuantityDelta(),
                saved.getReason(),
                saved.getOperatorId(),
                saved.getCreatedAt()
        );
    }
}
