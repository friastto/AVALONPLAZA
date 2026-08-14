package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.domain.inventory.application.dto.KardexResponseDto;
import org.frias.avalon.domain.inventory.infrastructure.entity.StockMovementEntity;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaStockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for GetKardexUseCaseImpl Application Service")
class GetKardexUseCaseImplTest {

    private JpaStockMovementRepository stockMovementRepository;
    private GetKardexUseCaseImpl getKardexUseCase;

    @BeforeEach
    void setUp() {
        stockMovementRepository = mock(JpaStockMovementRepository.class);
        getKardexUseCase = new GetKardexUseCaseImpl(stockMovementRepository);
    }

    @Test
    @DisplayName("Should return list of KardexResponseDto by product outlet id")
    void shouldReturnKardexByProductOutletId() {
        Long productOutletId = 10L;
        StockMovementEntity movement = new StockMovementEntity();
        movement.setId(1L);
        movement.setProductOutletId(productOutletId);
        movement.setOutletId(2L);
        movement.setMovementType("ADJUSTMENT");
        movement.setQuantityBefore(10);
        movement.setQuantityAfter(15);
        movement.setQuantityDelta(5);
        movement.setReason("Inventory recount");
        movement.setOperatorId(100L);
        movement.setCreatedAt(LocalDateTime.now());

        when(stockMovementRepository.findByProductOutletIdOrderByCreatedAtDesc(productOutletId))
                .thenReturn(List.of(movement));

        List<KardexResponseDto> results = getKardexUseCase.findByProductOutletId(productOutletId);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(productOutletId, results.get(0).productOutletId());
        assertEquals("ADJUSTMENT", results.get(0).movementType());
        verify(stockMovementRepository, times(1)).findByProductOutletIdOrderByCreatedAtDesc(productOutletId);
    }
}
