package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.inventory.application.dto.StockAdjustmentRequest;
import org.frias.avalon.domain.inventory.application.dto.StockAdjustmentResponse;
import org.frias.avalon.domain.inventory.application.event.StockAdjustmentNotificationEvent;
import org.frias.avalon.domain.inventory.infrastructure.entity.StockMovementEntity;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaStockMovementRepository;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementation of StockAdjustmentUseCase Input Port.
 */
@Service
public class StockAdjustmentUseCaseImpl implements StockAdjustmentUseCase {

    private final JpaProductOutletRepository productOutletRepository;
    private final JpaStockMovementRepository stockMovementRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UnitConversionService unitConversionService;
    private final MasterTreeProvider masterTreeProvider;

    public StockAdjustmentUseCaseImpl(
            JpaProductOutletRepository productOutletRepository,
            JpaStockMovementRepository stockMovementRepository,
            ApplicationEventPublisher eventPublisher,
            UnitConversionService unitConversionService,
            MasterTreeProvider masterTreeProvider
    ) {
        this.productOutletRepository = productOutletRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.eventPublisher = eventPublisher;
        this.unitConversionService = unitConversionService;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Transactional
    @Override
    public StockAdjustmentResponse execute(StockAdjustmentRequest request) {

        ProductOutlet productOutlet = productOutletRepository.findById(request.productOutletId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ProductOutlet not found for id: " + request.productOutletId()));

        MasterTree tree = masterTreeProvider != null ? masterTreeProvider.getTree() : null;
        MasterRoot unitNode = (tree != null && productOutlet.getUnitMeasureId() != null)
                ? tree.getById(productOutlet.getUnitMeasureId())
                : null;
        String unitCode = unitNode != null ? unitNode.getShortName() : "UND";

        BigDecimal qty = request.decimalQuantity() != null
                ? request.decimalQuantity()
                : (request.newQuantity() != null ? BigDecimal.valueOf(request.newQuantity()) : BigDecimal.ZERO);

        int after = (unitConversionService != null)
                ? unitConversionService.convertToSmallestUnit(qty, unitCode)
                : qty.intValue();

        int before = productOutlet.getStock();
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
