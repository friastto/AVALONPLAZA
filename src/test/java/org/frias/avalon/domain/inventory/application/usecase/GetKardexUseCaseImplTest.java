package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.domain.inventory.application.dto.KardexResponseDto;
import org.frias.avalon.domain.inventory.infrastructure.entity.StockMovementEntity;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaStockMovementRepository;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for GetKardexUseCaseImpl")
class GetKardexUseCaseImplTest {

    @Mock
    private JpaStockMovementRepository stockMovementRepository;
    @Mock
    private JpaOutletRepository jpaOutletRepository;
    @Mock
    private PlatformTransactionManager transactionManager;

    private GetKardexUseCaseImpl getKardexUseCase;

    @BeforeEach
    void setUp() {
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        lenient().when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        getKardexUseCase = new GetKardexUseCaseImpl(
                stockMovementRepository,
                jpaOutletRepository,
                transactionManager
        );
    }

    @Test
    @DisplayName("Should return kardex list by outletId")
    void shouldReturnKardexByOutletId() {
        Long outletId = 1L;
        Outlet outlet = new Outlet();
        outlet.setId(outletId);
        outlet.setCompanyId(10L);

        when(jpaOutletRepository.findById(outletId)).thenReturn(Optional.of(outlet));

        StockMovementEntity movement = StockMovementEntity.builder()
                .id(1L)
                .productOutletId(20L)
                .outletId(outletId)
                .movementType("SALE")
                .quantityBefore(10)
                .quantityAfter(9)
                .quantityDelta(-1)
                .reason("Venta POS")
                .operatorId(2L)
                .createdAt(LocalDateTime.now())
                .build();

        when(stockMovementRepository.findByOutletIdOrderByCreatedAtDesc(outletId)).thenReturn(List.of(movement));

        List<KardexResponseDto> result = getKardexUseCase.findByOutletId(outletId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("SALE", result.get(0).movementType());
    }
}
