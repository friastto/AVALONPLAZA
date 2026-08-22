package org.frias.avalon.domain.order.application.usecase;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderStatusHistoryDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.mapper.OrderMapper;
import org.frias.avalon.domain.order.presentation.controller.OrderWebSocketController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for ClaimOrderFifoUseCaseImpl")
class ClaimOrderFifoUseCaseImplTest {

    private OrderRepositoryPort orderRepositoryPort;
    private MasterDataRepositoryPort masterDataRepositoryPort;
    private OrderMapper orderMapper;
    private OrderWebSocketController orderWebSocketController;
    private ClaimOrderFifoUseCaseImpl claimOrderFifoUseCase;

    @BeforeEach
    void setUp() {
        orderRepositoryPort = mock(OrderRepositoryPort.class);
        masterDataRepositoryPort = mock(MasterDataRepositoryPort.class);
        orderMapper = mock(OrderMapper.class);
        orderWebSocketController = mock(OrderWebSocketController.class);

        claimOrderFifoUseCase = new ClaimOrderFifoUseCaseImpl(
                orderRepositoryPort,
                masterDataRepositoryPort,
                orderMapper,
                orderWebSocketController
        );
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no pending orders exist in FIFO queue")
    void shouldThrowExceptionWhenNoPendingOrdersInFifo() {
        Long outletId = 1L;
        Long userId = 10L;

        when(masterDataRepositoryPort.getIdByCode("ORD_PEN")).thenReturn(1L);
        when(orderRepositoryPort.findNextPendingOrderFifo(outletId, 1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                claimOrderFifoUseCase.execute(outletId, userId));

        assertTrue(exception.getMessage().contains("No hay pedidos pendientes en cola FIFO"));
        verify(orderRepositoryPort, never()).save(any());
        verify(orderWebSocketController, never()).broadcastOrderStatusChanged(any(), any());
    }

    @Test
    @DisplayName("Should claim next pending order in FIFO queue successfully and notify WebSocket listeners")
    void shouldClaimNextPendingOrderInFifoSuccessfully() {
        Long outletId = 1L;
        Long userId = 10L;
        Long pendingStatusId = 1L;
        Long processingStatusId = 2L;

        OrderDomain pendingOrder = OrderDomain.builder()
                .id(100L)
                .orderCode("ORD-100")
                .outletId(outletId)
                .orderStatusId(pendingStatusId)
                .total(new BigDecimal("150.00"))
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(100L)
                .orderCode("ORD-100")
                .orderStatusId(processingStatusId)
                .claimedByUserId(userId)
                .build();

        when(masterDataRepositoryPort.getIdByCode("ORD_PEN")).thenReturn(pendingStatusId);
        when(masterDataRepositoryPort.getIdByCode("ORD_REC")).thenReturn(processingStatusId);
        when(orderRepositoryPort.findNextPendingOrderFifo(outletId, pendingStatusId)).thenReturn(Optional.of(pendingOrder));
        when(orderRepositoryPort.save(any(OrderDomain.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(OrderDomain.class))).thenReturn(expectedResponse);

        OrderResponse result = claimOrderFifoUseCase.execute(outletId, userId);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(userId, result.getClaimedByUserId());
        assertEquals(processingStatusId, result.getOrderStatusId());

        verify(orderRepositoryPort, times(1)).save(any(OrderDomain.class));
        verify(orderRepositoryPort, times(1)).saveStatusHistory(any(OrderStatusHistoryDomain.class));
        verify(orderWebSocketController, times(1)).broadcastOrderStatusChanged(eq(100L), eq(expectedResponse));
    }
}
