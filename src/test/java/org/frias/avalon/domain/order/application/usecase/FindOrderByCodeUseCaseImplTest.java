package org.frias.avalon.domain.order.application.usecase;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for FindOrderByCodeUseCaseImpl")
class FindOrderByCodeUseCaseImplTest {

    private OrderRepositoryPort orderRepositoryPort;
    private OrderMapper orderMapper;
    private FindOrderByCodeUseCaseImpl findOrderByCodeUseCase;

    @BeforeEach
    void setUp() {
        orderRepositoryPort = mock(OrderRepositoryPort.class);
        orderMapper = mock(OrderMapper.class);
        findOrderByCodeUseCase = new FindOrderByCodeUseCaseImpl(orderRepositoryPort, orderMapper);
    }

    @Test
    @DisplayName("Should return OrderResponse when order code exists")
    void execute_Success_WhenOrderExists() {
        String code = "ORD-TEST-12345";
        OrderDomain order = OrderDomain.builder().id(10L).orderCode(code).build();
        OrderResponse response = OrderResponse.builder().id(10L).orderCode(code).build();

        when(orderRepositoryPort.findByOrderCode(code)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponse result = findOrderByCodeUseCase.execute(code);

        assertNotNull(result);
        assertEquals(code, result.getOrderCode());
        verify(orderRepositoryPort, times(1)).findByOrderCode(code);
        verify(orderMapper, times(1)).toResponse(order);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order code does not exist")
    void execute_ThrowsException_WhenOrderNotFound() {
        String code = "ORD-NOT-FOUND";
        when(orderRepositoryPort.findByOrderCode(code)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> findOrderByCodeUseCase.execute(code));
        verify(orderRepositoryPort, times(1)).findByOrderCode(code);
        verifyNoInteractions(orderMapper);
    }
}
