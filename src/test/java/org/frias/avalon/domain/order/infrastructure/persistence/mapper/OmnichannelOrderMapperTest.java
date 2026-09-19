package org.frias.avalon.domain.order.infrastructure.persistence.mapper;

import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for Omnichannel OrderMapper")
class OmnichannelOrderMapperTest {

    private JpaOutletRepository jpaOutletRepository;
    private OrderMapper orderMapper;

    @BeforeEach
    void setUp() {
        jpaOutletRepository = mock(JpaOutletRepository.class);
        orderMapper = new OrderMapper(
                null,
                null,
                null,
                null,
                null,
                jpaOutletRepository
        );
    }

    @Test
    @DisplayName("toResponse should return null when domain is null")
    void toResponse_WhenDomainIsNull_ReturnsNull() {
        assertNull(orderMapper.toResponse(null));
    }

    @Test
    @DisplayName("toResponse should map outletName when outletId is present")
    void toResponse_WhenOutletExists_MapsOutletName() {
        Long outletId = 1L;
        Outlet outlet = Outlet.builder()
                .id(outletId)
                .name("Avalon Plaza Principal")
                .build();
        when(jpaOutletRepository.findById(outletId)).thenReturn(Optional.of(outlet));

        OrderDomain domain = OrderDomain.builder()
                .id(100L)
                .orderCode("ORD-100")
                .outletId(outletId)
                .total(new BigDecimal("25000.00"))
                .build();

        OrderResponse response = orderMapper.toResponse(domain);

        assertNotNull(response);
        assertEquals("Avalon Plaza Principal", response.getOutletName());
        assertEquals(outletId, response.getOutletId());
    }

    @Test
    @DisplayName("toResponse should have null outletName when outlet is not found")
    void toResponse_WhenOutletNotFound_OutletNameIsNull() {
        Long outletId = 999L;
        when(jpaOutletRepository.findById(outletId)).thenReturn(Optional.empty());

        OrderDomain domain = OrderDomain.builder()
                .id(101L)
                .orderCode("ORD-101")
                .outletId(outletId)
                .build();

        OrderResponse response = orderMapper.toResponse(domain);

        assertNotNull(response);
        assertNull(response.getOutletName());
    }
}
