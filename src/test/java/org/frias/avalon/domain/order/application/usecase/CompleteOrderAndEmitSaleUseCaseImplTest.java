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

@DisplayName("Unit Tests for CompleteOrderAndEmitSaleUseCaseImpl in Omnichannel Order Domain")
class CompleteOrderAndEmitSaleUseCaseImplTest {

    private OrderRepositoryPort orderRepositoryPort;
    private MasterDataRepositoryPort masterDataRepositoryPort;
    private OrderMapper orderMapper;
    private OrderWebSocketController orderWebSocketController;

    private CompleteOrderAndEmitSaleUseCaseImpl completeOrderAndEmitSaleUseCase;

    @BeforeEach
    void setUp() {
        orderRepositoryPort = mock(OrderRepositoryPort.class);
        masterDataRepositoryPort = mock(MasterDataRepositoryPort.class);
        orderMapper = mock(OrderMapper.class);
        orderWebSocketController = mock(OrderWebSocketController.class);

        completeOrderAndEmitSaleUseCase = new CompleteOrderAndEmitSaleUseCaseImpl(
                orderRepositoryPort,
                masterDataRepositoryPort,
                orderMapper,
                orderWebSocketController
        );
    }

    @Test
    @DisplayName("Should complete order and update payment status when ORD_DEL and PAY_PAD master data exist")
    void execute_Success_WhenOrdDelAndPayPadFound() {
        Long orderId = 100L;
        Long userId = 77L;
        Long previousStatusId = 2L;
        Long deliveredStatusId = 301L;
        Long paidStatusId = 302L;

        OrderDomain initialOrder = OrderDomain.builder()
                .id(orderId)
                .orderCode("ORD-2026-888")
                .orderStatusId(previousStatusId)
                .paymentStatusId(1L)
                .total(new BigDecimal("250.00"))
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(initialOrder));
        when(masterDataRepositoryPort.getIdByCode("ORD_DEL")).thenReturn(deliveredStatusId);
        when(masterDataRepositoryPort.getIdByCode("PAY_PAD")).thenReturn(paidStatusId);

        OrderDomain savedOrder = OrderDomain.builder()
                .id(orderId)
                .orderCode("ORD-2026-888")
                .orderStatusId(deliveredStatusId)
                .paymentStatusId(paidStatusId)
                .total(new BigDecimal("250.00"))
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepositoryPort.save(any(OrderDomain.class))).thenReturn(savedOrder);

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .orderCode("ORD-2026-888")
                .orderStatusId(deliveredStatusId)
                .paymentStatusId(paidStatusId)
                .build();

        when(orderMapper.toResponse(savedOrder)).thenReturn(expectedResponse);

        OrderResponse result = completeOrderAndEmitSaleUseCase.execute(orderId, userId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(deliveredStatusId, result.getOrderStatusId());
        assertEquals(paidStatusId, result.getPaymentStatusId());

        ArgumentCaptor<OrderDomain> orderCaptor = ArgumentCaptor.forClass(OrderDomain.class);
        verify(orderRepositoryPort).save(orderCaptor.capture());
        OrderDomain capturedOrder = orderCaptor.getValue();
        assertEquals(deliveredStatusId, capturedOrder.getOrderStatusId());
        assertEquals(paidStatusId, capturedOrder.getPaymentStatusId());
        assertNotNull(capturedOrder.getUpdatedAt());

        ArgumentCaptor<OrderStatusHistoryDomain> historyCaptor = ArgumentCaptor.forClass(OrderStatusHistoryDomain.class);
        verify(orderRepositoryPort).saveStatusHistory(historyCaptor.capture());
        OrderStatusHistoryDomain capturedHistory = historyCaptor.getValue();
        assertEquals(orderId, capturedHistory.getOrderId());
        assertEquals(previousStatusId, capturedHistory.getPreviousStatusId());
        assertEquals(deliveredStatusId, capturedHistory.getNewStatusId());
        assertEquals(userId, capturedHistory.getChangedByUserId());
        assertTrue(capturedHistory.getNotes().contains("usuario 77"));
        assertNotNull(capturedHistory.getCreatedAt());

        verify(orderWebSocketController, times(1)).broadcastOrderStatusChanged(eq(orderId), eq(expectedResponse));
    }

    @Test
    @DisplayName("Should complete order using fallback COM code and default PAY_PAD status")
    void execute_Success_WhenFallbackComAndDefaultPayPad() {
        Long orderId = 200L;
        Long userId = 10L;
        Long fallbackDeliveredStatusId = 303L;

        OrderDomain initialOrder = OrderDomain.builder()
                .id(orderId)
                .orderStatusId(2L)
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(initialOrder));
        when(masterDataRepositoryPort.getIdByCode("ORD_DEL")).thenReturn(null);
        when(masterDataRepositoryPort.getIdByCode("COM")).thenReturn(fallbackDeliveredStatusId);
        when(masterDataRepositoryPort.getIdByCode("PAY_PAD")).thenReturn(null);

        OrderDomain savedOrder = OrderDomain.builder()
                .id(orderId)
                .orderStatusId(fallbackDeliveredStatusId)
                .paymentStatusId(2L)
                .build();

        when(orderRepositoryPort.save(any(OrderDomain.class))).thenReturn(savedOrder);

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .orderStatusId(fallbackDeliveredStatusId)
                .paymentStatusId(2L)
                .build();

        when(orderMapper.toResponse(savedOrder)).thenReturn(expectedResponse);

        OrderResponse result = completeOrderAndEmitSaleUseCase.execute(orderId, userId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(fallbackDeliveredStatusId, result.getOrderStatusId());
        assertEquals(2L, result.getPaymentStatusId());
    }

    @Test
    @DisplayName("Should complete order using default status IDs (3L and 2L) when all master data returns null")
    void execute_Success_WhenFallbackDefaultStatusIds() {
        Long orderId = 300L;
        Long userId = 5L;

        OrderDomain initialOrder = OrderDomain.builder()
                .id(orderId)
                .orderStatusId(1L)
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(initialOrder));
        when(masterDataRepositoryPort.getIdByCode("ORD_DEL")).thenReturn(null);
        when(masterDataRepositoryPort.getIdByCode("COM")).thenReturn(null);
        when(masterDataRepositoryPort.getIdByCode("PAY_PAD")).thenReturn(null);

        OrderDomain savedOrder = OrderDomain.builder()
                .id(orderId)
                .orderStatusId(3L)
                .paymentStatusId(2L)
                .build();

        when(orderRepositoryPort.save(any(OrderDomain.class))).thenReturn(savedOrder);

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .orderStatusId(3L)
                .paymentStatusId(2L)
                .build();

        when(orderMapper.toResponse(savedOrder)).thenReturn(expectedResponse);

        OrderResponse result = completeOrderAndEmitSaleUseCase.execute(orderId, userId);

        assertNotNull(result);
        assertEquals(3L, result.getOrderStatusId());
        assertEquals(2L, result.getPaymentStatusId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when target order is not found")
    void execute_ThrowsResourceNotFoundException_WhenOrderNotFound() {
        Long orderId = 999L;
        Long userId = 1L;

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> completeOrderAndEmitSaleUseCase.execute(orderId, userId)
        );

        assertTrue(exception.getMessage().contains("Pedido con ID 999 no encontrado"));
        verify(orderRepositoryPort, never()).save(any());
        verify(orderRepositoryPort, never()).saveStatusHistory(any());
        verify(orderWebSocketController, never()).broadcastOrderStatusChanged(any(), any());
    }
}
