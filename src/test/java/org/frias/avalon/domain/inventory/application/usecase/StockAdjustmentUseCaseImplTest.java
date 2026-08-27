package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.inventory.application.dto.StockAdjustmentRequest;
import org.frias.avalon.domain.inventory.application.dto.StockAdjustmentResponse;
import org.frias.avalon.domain.inventory.application.event.StockAdjustmentNotificationEvent;
import org.frias.avalon.domain.inventory.infrastructure.entity.StockMovementEntity;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaStockMovementRepository;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for StockAdjustmentUseCaseImpl in Inventory Domain")
class StockAdjustmentUseCaseImplTest {

    private JpaProductOutletRepository productOutletRepository;
    private JpaStockMovementRepository stockMovementRepository;
    private ApplicationEventPublisher eventPublisher;

    private StockAdjustmentUseCaseImpl stockAdjustmentUseCase;

    @BeforeEach
    void setUp() {
        productOutletRepository = mock(JpaProductOutletRepository.class);
        stockMovementRepository = mock(JpaStockMovementRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        stockAdjustmentUseCase = new StockAdjustmentUseCaseImpl(
                productOutletRepository,
                stockMovementRepository,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product outlet does not exist")
    void shouldThrowExceptionWhenProductOutletNotFound() {
        StockAdjustmentRequest request = new StockAdjustmentRequest(99L, 1L, 10, "Inventario fisico", 5L);

        when(productOutletRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> stockAdjustmentUseCase.execute(request));
    }

    @Test
    @DisplayName("Should execute stock adjustment surplus and publish notification event")
    void shouldExecuteStockAdjustmentSurplusSuccessfully() {
        StockAdjustmentRequest request = new StockAdjustmentRequest(10L, 1L, 15, "Ajuste de inventario fisico", 5L);

        ProductOutlet productOutlet = new ProductOutlet();
        productOutlet.setId(10L);
        productOutlet.setStock(10);
        productOutlet.setLocalName("Arroz 1KG");

        when(productOutletRepository.findById(10L)).thenReturn(Optional.of(productOutlet));

        StockMovementEntity savedEntity = StockMovementEntity.builder()
                .id(100L)
                .productOutletId(10L)
                .outletId(1L)
                .movementType("ADJUSTMENT_SURPLUS")
                .quantityBefore(10)
                .quantityAfter(15)
                .quantityDelta(5)
                .reason("Ajuste de inventario fisico")
                .operatorId(5L)
                .createdAt(LocalDateTime.now())
                .build();

        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenReturn(savedEntity);

        StockAdjustmentResponse response = stockAdjustmentUseCase.execute(request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("ADJUSTMENT_SURPLUS", response.movementType());
        assertEquals(5, response.quantityDelta());
        assertEquals(15, productOutlet.getStock());

        verify(productOutletRepository, times(1)).save(productOutlet);
        verify(stockMovementRepository, times(1)).save(any(StockMovementEntity.class));
        verify(eventPublisher, times(1)).publishEvent(any(StockAdjustmentNotificationEvent.class));
    }
}
