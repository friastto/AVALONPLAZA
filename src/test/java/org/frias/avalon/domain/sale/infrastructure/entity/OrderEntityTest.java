package org.frias.avalon.domain.sale.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para OrderEntity.
 */
@DisplayName("Unit Tests for OrderEntity")
class OrderEntityTest {

    @Test
    @DisplayName("NoArgsConstructor and Getters/Setters should set and retrieve all properties correctly")
    void noArgsConstructorAndGettersSetters_WorkCorrectly() {
        OrderEntity entity = new OrderEntity();

        Long id = 1L;
        UUID orderCode = UUID.randomUUID();
        BigDecimal totalAmount = new BigDecimal("100.00");
        Long paymentMethodId = 2L;
        Long statusId = 3L;
        Long outletId = 4L;
        LocalDateTime orderDate = LocalDateTime.now();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        List<OrderItemEntity> items = new ArrayList<>();

        entity.setId(id);
        entity.setOrderCode(orderCode);
        entity.setTotalAmount(totalAmount);
        entity.setPaymentMethodId(paymentMethodId);
        entity.setStatusId(statusId);
        entity.setOutletId(outletId);
        entity.setOrderDate(orderDate);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        entity.setItems(items);

        assertEquals(id, entity.getId());
        assertEquals(orderCode, entity.getOrderCode());
        assertEquals(totalAmount, entity.getTotalAmount());
        assertEquals(paymentMethodId, entity.getPaymentMethodId());
        assertEquals(statusId, entity.getStatusId());
        assertEquals(outletId, entity.getOutletId());
        assertEquals(orderDate, entity.getOrderDate());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
        assertEquals(items, entity.getItems());
    }

    @Test
    @DisplayName("AllArgsConstructor and Builder should initialize all fields correctly")
    void allArgsConstructorAndBuilder_WorkCorrectly() {
        Long id = 10L;
        UUID orderCode = UUID.randomUUID();
        BigDecimal totalAmount = new BigDecimal("250.50");
        Long paymentMethodId = 1L;
        Long statusId = 2L;
        Long outletId = 5L;
        LocalDateTime orderDate = LocalDateTime.now();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        List<OrderItemEntity> items = List.of(new OrderItemEntity());

        OrderEntity entityFromBuilder = OrderEntity.builder()
                .id(id)
                .orderCode(orderCode)
                .totalAmount(totalAmount)
                .paymentMethodId(paymentMethodId)
                .statusId(statusId)
                .outletId(outletId)
                .orderDate(orderDate)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .items(items)
                .build();

        assertEquals(id, entityFromBuilder.getId());
        assertEquals(orderCode, entityFromBuilder.getOrderCode());
        assertEquals(totalAmount, entityFromBuilder.getTotalAmount());
        assertEquals(paymentMethodId, entityFromBuilder.getPaymentMethodId());
        assertEquals(statusId, entityFromBuilder.getStatusId());
        assertEquals(outletId, entityFromBuilder.getOutletId());
        assertEquals(orderDate, entityFromBuilder.getOrderDate());
        assertEquals(createdAt, entityFromBuilder.getCreatedAt());
        assertEquals(updatedAt, entityFromBuilder.getUpdatedAt());
        assertEquals(items, entityFromBuilder.getItems());

        OrderEntity entityFromAllArgs = new OrderEntity(
                id, orderCode, totalAmount, paymentMethodId, statusId, outletId,
                orderDate, createdAt, updatedAt, items
        );

        assertEquals(id, entityFromAllArgs.getId());
        assertEquals(orderCode, entityFromAllArgs.getOrderCode());
    }

    @Test
    @DisplayName("onCreate should initialize orderCode, orderDate, createdAt, and updatedAt when fields are null")
    void onCreate_WhenFieldsAreNull_PopulatesDefaultCodeDateAndTimestamps() {
        OrderEntity entity = new OrderEntity();

        assertNull(entity.getOrderCode());
        assertNull(entity.getOrderDate());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());

        entity.onCreate();

        assertNotNull(entity.getOrderCode());
        assertNotNull(entity.getOrderDate());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("onCreate should preserve existing orderCode and orderDate if they are already present")
    void onCreate_WhenFieldsAreAlreadySet_PreservesExistingCodeAndDate() {
        UUID existingCode = UUID.randomUUID();
        LocalDateTime existingOrderDate = LocalDateTime.now().minusDays(2);

        OrderEntity entity = OrderEntity.builder()
                .orderCode(existingCode)
                .orderDate(existingOrderDate)
                .build();

        entity.onCreate();

        assertEquals(existingCode, entity.getOrderCode());
        assertEquals(existingOrderDate, entity.getOrderDate());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("onUpdate should refresh updatedAt timestamp")
    void onUpdate_UpdatesUpdatedAtTimestamp() throws InterruptedException {
        OrderEntity entity = new OrderEntity();
        LocalDateTime past = LocalDateTime.now().minusHours(1);
        entity.setUpdatedAt(past);

        entity.onUpdate();

        assertNotNull(entity.getUpdatedAt());
        assertTrue(entity.getUpdatedAt().isAfter(past) || entity.getUpdatedAt().isEqual(past));
    }
}
