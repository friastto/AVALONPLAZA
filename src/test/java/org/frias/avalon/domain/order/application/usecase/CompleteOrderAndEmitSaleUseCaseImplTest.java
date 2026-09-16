package org.frias.avalon.domain.order.application.usecase;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderStatusHistoryDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.mapper.OrderMapper;
import org.frias.avalon.domain.order.presentation.controller.OrderWebSocketController;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for CompleteOrderAndEmitSaleUseCaseImpl in Omnichannel Order Domain")
class CompleteOrderAndEmitSaleUseCaseImplTest {

    private OrderRepositoryPort orderRepositoryPort;
    private MasterTreeProvider masterTreeProvider;
    private OrderMapper orderMapper;
    private OrderWebSocketController orderWebSocketController;
    private JpaProductOutletRepository jpaProductOutletRepository;

    private CompleteOrderAndEmitSaleUseCaseImpl completeOrderAndEmitSaleUseCase;

    @BeforeEach
    void setUp() {
        orderRepositoryPort = mock(OrderRepositoryPort.class);
        masterTreeProvider = mock(MasterTreeProvider.class);
        orderMapper = mock(OrderMapper.class);
        orderWebSocketController = mock(OrderWebSocketController.class);
        jpaProductOutletRepository = mock(JpaProductOutletRepository.class);

        completeOrderAndEmitSaleUseCase = new CompleteOrderAndEmitSaleUseCaseImpl(
                orderRepositoryPort,
                masterTreeProvider,
                jpaProductOutletRepository,
                orderMapper,
                orderWebSocketController
        );
    }

    @Test
    @DisplayName("Should complete order and set status to ORD_DISP when ORD_DISP master data exists")
    void execute_Success_WhenOrdDispFound() {
        Long orderId = 100L;
        Long userId = 77L;
        Long previousStatusId = 2L;
        Long dispStatusId = 301L;

        MasterRoot dispNode = new MasterRoot(dispStatusId, "ORD_DISP", "DESPACHADO", null, 1L);
        MasterTree tree = new MasterTree(List.of(dispNode));
        when(masterTreeProvider.getTree()).thenReturn(tree);

        OrderDomain initialOrder = OrderDomain.builder()
                .id(orderId)
                .orderCode("ORD-2026-888")
                .orderStatusId(previousStatusId)
                .paymentStatusId(1L)
                .total(new BigDecimal("250.00"))
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(initialOrder));

        OrderDomain savedOrder = OrderDomain.builder()
                .id(orderId)
                .orderCode("ORD-2026-888")
                .orderStatusId(dispStatusId)
                .paymentStatusId(1L)
                .total(new BigDecimal("250.00"))
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepositoryPort.save(any(OrderDomain.class))).thenReturn(savedOrder);

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .orderCode("ORD-2026-888")
                .orderStatusId(dispStatusId)
                .paymentStatusId(1L)
                .build();

        when(orderMapper.toResponse(savedOrder)).thenReturn(expectedResponse);

        OrderResponse result = completeOrderAndEmitSaleUseCase.execute(orderId, userId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(dispStatusId, result.getOrderStatusId());
        assertEquals(1L, result.getPaymentStatusId());

        ArgumentCaptor<OrderDomain> orderCaptor = ArgumentCaptor.forClass(OrderDomain.class);
        verify(orderRepositoryPort).save(orderCaptor.capture());
        OrderDomain capturedOrder = orderCaptor.getValue();
        assertEquals(dispStatusId, capturedOrder.getOrderStatusId());
        assertEquals(1L, capturedOrder.getPaymentStatusId());
        assertNotNull(capturedOrder.getUpdatedAt());

        ArgumentCaptor<OrderStatusHistoryDomain> historyCaptor = ArgumentCaptor.forClass(OrderStatusHistoryDomain.class);
        verify(orderRepositoryPort).saveStatusHistory(historyCaptor.capture());
        OrderStatusHistoryDomain capturedHistory = historyCaptor.getValue();
        assertEquals(orderId, capturedHistory.getOrderId());
        assertEquals(previousStatusId, capturedHistory.getPreviousStatusId());
        assertEquals(dispStatusId, capturedHistory.getNewStatusId());
        assertEquals(userId, capturedHistory.getChangedByUserId());
        assertTrue(capturedHistory.getNotes().contains("despachado por el usuario 77"));
        assertNotNull(capturedHistory.getCreatedAt());

        verify(orderWebSocketController, times(1)).broadcastOrderStatusChanged(eq(orderId), eq(expectedResponse));
    }

    @Test
    @DisplayName("Should complete order using fallback COM code when ORD_DISP does not exist")
    void execute_Success_WhenFallbackCom() {
        Long orderId = 200L;
        Long userId = 10L;
        Long fallbackDeliveredStatusId = 303L;

        MasterRoot comNode = new MasterRoot(fallbackDeliveredStatusId, "COM", "COMPLETADO", null, 1L);
        MasterTree tree = new MasterTree(List.of(comNode));
        when(masterTreeProvider.getTree()).thenReturn(tree);

        OrderDomain initialOrder = OrderDomain.builder()
                .id(orderId)
                .orderStatusId(2L)
                .paymentStatusId(1L)
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(initialOrder));

        OrderDomain savedOrder = OrderDomain.builder()
                .id(orderId)
                .orderStatusId(fallbackDeliveredStatusId)
                .paymentStatusId(1L)
                .build();

        when(orderRepositoryPort.save(any(OrderDomain.class))).thenReturn(savedOrder);

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .orderStatusId(fallbackDeliveredStatusId)
                .paymentStatusId(1L)
                .build();

        when(orderMapper.toResponse(savedOrder)).thenReturn(expectedResponse);

        OrderResponse result = completeOrderAndEmitSaleUseCase.execute(orderId, userId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(fallbackDeliveredStatusId, result.getOrderStatusId());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when neither ORD_DISP nor COM exist in MasterTree")
    void execute_ThrowsIllegalStateException_WhenNoDispOrComInMasterTree() {
        Long orderId = 300L;
        Long userId = 5L;

        MasterTree tree = new MasterTree(List.of());
        when(masterTreeProvider.getTree()).thenReturn(tree);

        OrderDomain initialOrder = OrderDomain.builder()
                .id(orderId)
                .orderStatusId(1L)
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(initialOrder));

        assertThrows(
                IllegalStateException.class,
                () -> completeOrderAndEmitSaleUseCase.execute(orderId, userId)
        );
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
