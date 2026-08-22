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

@DisplayName("Unit Tests for CompleteOrderAndEmitSaleUseCaseImpl")
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
    @DisplayName("Should throw ResourceNotFoundException when order to complete is not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        Long orderId = 999L;
        Long userId = 10L;

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                completeOrderAndEmitSaleUseCase.execute(orderId, userId));

        assertTrue(exception.getMessage().contains("Pedido con ID 999 no encontrado"));
        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should complete order, update payment status, save history and broadcast status change")
    void shouldCompleteOrderSuccessfully() {
        Long orderId = 100L;
        Long userId = 10L;
        Long deliveredStatusId = 3L;
        Long paidStatusId = 2L;

        OrderDomain order = OrderDomain.builder()
                .id(orderId)
                .orderCode("ORD-100")
                .orderStatusId(2L)
                .paymentStatusId(1L)
                .total(new BigDecimal("200.00"))
                .build();

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .orderCode("ORD-100")
                .orderStatusId(deliveredStatusId)
                .paymentStatusId(paidStatusId)
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));
        when(masterDataRepositoryPort.getIdByCode("ORD_DEL")).thenReturn(deliveredStatusId);
        when(masterDataRepositoryPort.getIdByCode("PAY_PAD")).thenReturn(paidStatusId);
        when(orderRepositoryPort.save(any(OrderDomain.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(OrderDomain.class))).thenReturn(expectedResponse);

        OrderResponse result = completeOrderAndEmitSaleUseCase.execute(orderId, userId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(deliveredStatusId, result.getOrderStatusId());
        assertEquals(paidStatusId, result.getPaymentStatusId());

        verify(orderRepositoryPort, times(1)).save(any(OrderDomain.class));
        verify(orderRepositoryPort, times(1)).saveStatusHistory(any(OrderStatusHistoryDomain.class));
        verify(orderWebSocketController, times(1)).broadcastOrderStatusChanged(eq(orderId), eq(expectedResponse));
    }
}
