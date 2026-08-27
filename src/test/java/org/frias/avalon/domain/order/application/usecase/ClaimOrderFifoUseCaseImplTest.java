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
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for ClaimOrderFifoUseCaseImpl in Omnichannel Order Domain")
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
    @DisplayName("Should successfully claim next FIFO pending order when master status codes ORD_PEN and ORD_REC are found")
    void execute_Success_WhenOrdPenAndOrdRecFoundInMasterData() {
        Long outletId = 10L;
        Long userId = 42L;
        Long pendingStatusId = 101L;
        Long receivedStatusId = 102L;
        Long orderId = 500L;

        when(masterDataRepositoryPort.getIdByCode("ORD_PEN")).thenReturn(pendingStatusId);
        when(masterDataRepositoryPort.getIdByCode("ORD_REC")).thenReturn(receivedStatusId);

        OrderDomain initialPendingOrder = OrderDomain.builder()
                .id(orderId)
                .orderCode("ORD-2026-001")
                .outletId(outletId)
                .orderStatusId(pendingStatusId)
                .total(new BigDecimal("150.00"))
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        when(orderRepositoryPort.findNextPendingOrderFifo(outletId, pendingStatusId))
                .thenReturn(Optional.of(initialPendingOrder));

        OrderDomain savedOrder = OrderDomain.builder()
                .id(orderId)
                .orderCode("ORD-2026-001")
                .outletId(outletId)
                .orderStatusId(receivedStatusId)
                .claimedByUserId(userId)
                .total(new BigDecimal("150.00"))
                .createdAt(initialPendingOrder.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepositoryPort.save(any(OrderDomain.class))).thenReturn(savedOrder);

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .orderCode("ORD-2026-001")
                .outletId(outletId)
                .orderStatusId(receivedStatusId)
                .claimedByUserId(userId)
                .total(new BigDecimal("150.00"))
                .build();

        when(orderMapper.toResponse(savedOrder)).thenReturn(expectedResponse);

        OrderResponse result = claimOrderFifoUseCase.execute(outletId, userId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(receivedStatusId, result.getOrderStatusId());
        assertEquals(userId, result.getClaimedByUserId());

        ArgumentCaptor<OrderDomain> orderCaptor = ArgumentCaptor.forClass(OrderDomain.class);
        verify(orderRepositoryPort).save(orderCaptor.capture());
        OrderDomain capturedOrder = orderCaptor.getValue();
        assertEquals(receivedStatusId, capturedOrder.getOrderStatusId());
        assertEquals(userId, capturedOrder.getClaimedByUserId());
        assertNotNull(capturedOrder.getUpdatedAt());

        ArgumentCaptor<OrderStatusHistoryDomain> historyCaptor = ArgumentCaptor.forClass(OrderStatusHistoryDomain.class);
        verify(orderRepositoryPort).saveStatusHistory(historyCaptor.capture());
        OrderStatusHistoryDomain capturedHistory = historyCaptor.getValue();
        assertEquals(orderId, capturedHistory.getOrderId());
        assertEquals(pendingStatusId, capturedHistory.getPreviousStatusId());
        assertEquals(receivedStatusId, capturedHistory.getNewStatusId());
        assertEquals(userId, capturedHistory.getChangedByUserId());
        assertTrue(capturedHistory.getNotes().contains("usuario 42"));
        assertNotNull(capturedHistory.getCreatedAt());

        verify(orderWebSocketController, times(1)).broadcastOrderStatusChanged(eq(orderId), eq(expectedResponse));
    }

    @Test
    @DisplayName("Should claim order using fallback codes PEN and PRO when ORD_PEN and ORD_REC are missing")
    void execute_Success_WhenFallbackPenAndProFoundInMasterData() {
        Long outletId = 5L;
        Long userId = 15L;
        Long fallbackPenStatusId = 201L;
        Long fallbackProStatusId = 202L;
        Long orderId = 300L;

        when(masterDataRepositoryPort.getIdByCode("ORD_PEN")).thenReturn(null);
        when(masterDataRepositoryPort.getIdByCode("PEN")).thenReturn(fallbackPenStatusId);

        when(masterDataRepositoryPort.getIdByCode("ORD_REC")).thenReturn(null);
        when(masterDataRepositoryPort.getIdByCode("PRO")).thenReturn(fallbackProStatusId);

        OrderDomain initialPendingOrder = OrderDomain.builder()
                .id(orderId)
                .orderCode("ORD-2026-002")
                .outletId(outletId)
                .orderStatusId(fallbackPenStatusId)
                .build();

        when(orderRepositoryPort.findNextPendingOrderFifo(outletId, fallbackPenStatusId))
                .thenReturn(Optional.of(initialPendingOrder));

        OrderDomain savedOrder = OrderDomain.builder()
                .id(orderId)
                .orderCode("ORD-2026-002")
                .outletId(outletId)
                .orderStatusId(fallbackProStatusId)
                .claimedByUserId(userId)
                .build();

        when(orderRepositoryPort.save(any(OrderDomain.class))).thenReturn(savedOrder);

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .orderStatusId(fallbackProStatusId)
                .build();

        when(orderMapper.toResponse(savedOrder)).thenReturn(expectedResponse);

        OrderResponse result = claimOrderFifoUseCase.execute(outletId, userId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        verify(orderRepositoryPort).findNextPendingOrderFifo(outletId, fallbackPenStatusId);
        verify(orderWebSocketController).broadcastOrderStatusChanged(orderId, expectedResponse);
    }

    @Test
    @DisplayName("Should claim order using default status IDs 1L and 2L when all master data codes return null")
    void execute_Success_WhenFallbackToDefaultIds() {
        Long outletId = 1L;
        Long userId = 99L;
        Long defaultPendingStatusId = 1L;
        Long defaultReceivedStatusId = 2L;
        Long orderId = 100L;

        when(masterDataRepositoryPort.getIdByCode("ORD_PEN")).thenReturn(null);
        when(masterDataRepositoryPort.getIdByCode("PEN")).thenReturn(null);

        when(masterDataRepositoryPort.getIdByCode("ORD_REC")).thenReturn(null);
        when(masterDataRepositoryPort.getIdByCode("PRO")).thenReturn(null);

        OrderDomain initialPendingOrder = OrderDomain.builder()
                .id(orderId)
                .outletId(outletId)
                .orderStatusId(defaultPendingStatusId)
                .build();

        when(orderRepositoryPort.findNextPendingOrderFifo(outletId, defaultPendingStatusId))
                .thenReturn(Optional.of(initialPendingOrder));

        OrderDomain savedOrder = OrderDomain.builder()
                .id(orderId)
                .outletId(outletId)
                .orderStatusId(defaultReceivedStatusId)
                .claimedByUserId(userId)
                .build();

        when(orderRepositoryPort.save(any(OrderDomain.class))).thenReturn(savedOrder);

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .orderStatusId(defaultReceivedStatusId)
                .build();

        when(orderMapper.toResponse(savedOrder)).thenReturn(expectedResponse);

        OrderResponse result = claimOrderFifoUseCase.execute(outletId, userId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        verify(orderRepositoryPort).findNextPendingOrderFifo(outletId, 1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no pending order exists in FIFO queue")
    void execute_ThrowsResourceNotFoundException_WhenNoPendingOrderInFifoQueue() {
        Long outletId = 999L;
        Long userId = 1L;

        when(masterDataRepositoryPort.getIdByCode("ORD_PEN")).thenReturn(101L);
        when(orderRepositoryPort.findNextPendingOrderFifo(outletId, 101L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> claimOrderFifoUseCase.execute(outletId, userId)
        );

        assertTrue(exception.getMessage().contains("No hay pedidos pendientes en cola FIFO para el outlet 999"));
        verify(orderRepositoryPort, never()).save(any());
        verify(orderRepositoryPort, never()).saveStatusHistory(any());
        verify(orderWebSocketController, never()).broadcastOrderStatusChanged(any(), any());
    }
}
