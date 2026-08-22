package org.frias.avalon.domain.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for OrderDomain Model")
class OrderDomainTest {

    @Test
    @DisplayName("Should build OrderDomain instance correctly using Builder pattern")
    void shouldBuildOrderDomainCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        OrderDomain order = OrderDomain.builder()
                .id(100L)
                .orderCode("ORD-2026-001")
                .customerId(10L)
                .outletId(1L)
                .orderStatusId(1L)
                .paymentStatusId(1L)
                .paymentMethodId(2L)
                .subtotal(new BigDecimal("100.00"))
                .tax(new BigDecimal("18.00"))
                .total(new BigDecimal("118.00"))
                .claimedByUserId(5L)
                .createdAt(now)
                .updatedAt(now)
                .items(new ArrayList<>())
                .build();

        assertNotNull(order);
        assertEquals(100L, order.getId());
        assertEquals("ORD-2026-001", order.getOrderCode());
        assertEquals(10L, order.getCustomerId());
        assertEquals(1L, order.getOutletId());
        assertEquals(new BigDecimal("118.00"), order.getTotal());
        assertEquals(5L, order.getClaimedByUserId());
        assertNotNull(order.getItems());
    }

    @Test
    @DisplayName("Should verify OrderItemDomain property mutators and accessors")
    void shouldMutateOrderItemDomainProperties() {
        OrderItemDomain item = OrderItemDomain.builder()
                .id(50L)
                .orderId(100L)
                .productOutletId(200L)
                .productName("Milk 1L")
                .quantity(2)
                .unitPrice(new BigDecimal("25.00"))
                .subtotal(new BigDecimal("50.00"))
                .dispatchStatusId(1L)
                .build();

        assertNotNull(item);
        assertEquals(50L, item.getId());
        assertEquals(100L, item.getOrderId());
        assertEquals(200L, item.getProductOutletId());
        assertEquals(new BigDecimal("50.00"), item.getSubtotal());

        item.setDispatchStatusId(2L);
        assertEquals(2L, item.getDispatchStatusId());
    }
}
