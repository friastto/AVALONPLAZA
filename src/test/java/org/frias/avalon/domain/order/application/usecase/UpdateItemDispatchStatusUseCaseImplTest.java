package org.frias.avalon.domain.order.application.usecase;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderItemDomain;
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

@DisplayName("Unit Tests for UpdateItemDispatchStatusUseCaseImpl")
class UpdateItemDispatchStatusUseCaseImplTest {

    private OrderRepositoryPort orderRepositoryPort;
    private OrderMapper orderMapper;
    private OrderWebSocketController orderWebSocketController;
    private UpdateItemDispatchStatusUseCaseImpl updateItemDispatchStatusUseCase;

    @BeforeEach
    void setUp() {
        orderRepositoryPort = mock(OrderRepositoryPort.class);
        orderMapper = mock(OrderMapper.class);
        orderWebSocketController = mock(OrderWebSocketController.class);

        updateItemDispatchStatusUseCase = new UpdateItemDispatchStatusUseCaseImpl(
                orderRepositoryPort,
                orderMapper,
                orderWebSocketController
        );
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order ID is not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        Long orderId = 999L;
        Long itemId = 50L;
        Long statusId = 2L;

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                updateItemDispatchStatusUseCase.execute(orderId, itemId, statusId));

        assertTrue(exception.getMessage().contains("Pedido con ID 999 no encontrado"));
        verify(orderRepositoryPort, never()).saveItem(any());
    }

    @Test
    @DisplayName("Should update item dispatch status successfully and broadcast change via WebSocket")
    void shouldUpdateItemDispatchStatusSuccessfully() {
        Long orderId = 100L;
        Long itemId = 50L;
        Long newStatusId = 102L;

        OrderDomain order = OrderDomain.builder()
                .id(orderId)
                .orderCode("ORD-100")
                .orderStatusId(2L)
                .build();

        OrderItemDomain item = OrderItemDomain.builder()
                .id(itemId)
                .orderId(orderId)
                .productOutletId(200L)
                .productName("Milk 1L")
                .quantity(1)
                .dispatchStatusId(100L)
                .build();

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .orderCode("ORD-100")
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepositoryPort.findItemById(itemId)).thenReturn(Optional.of(item));
        when(orderRepositoryPort.saveItem(any(OrderItemDomain.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(OrderDomain.class))).thenReturn(expectedResponse);

        OrderResponse result = updateItemDispatchStatusUseCase.execute(orderId, itemId, newStatusId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());

        verify(orderRepositoryPort, times(1)).saveItem(argThat(updatedItem ->
                updatedItem.getId().equals(itemId) && updatedItem.getDispatchStatusId().equals(newStatusId)
        ));
        verify(orderWebSocketController, times(1)).broadcastOrderStatusChanged(eq(orderId), eq(expectedResponse));
    }
}
