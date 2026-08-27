package org.frias.avalon.domain.sale.domain;

import org.frias.avalon.core.exeptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for OrderDomain")
class OrderDomainTest {

    private OrderItemDomain createSampleItem(Long productId, BigDecimal subtotal) {
        return new OrderItemDomain(
                null,
                productId,
                1,
                "1 UND",
                subtotal,
                subtotal,
                1L
        );
    }

    @Test
    @DisplayName("Should create OrderDomain successfully and compute total amount")
    void shouldCreateOrderDomainSuccessfully() {
        Long paymentMethodId = 1L;
        Long statusId = 2L;
        Long outletId = 3L;

        OrderItemDomain item1 = createSampleItem(10L, new BigDecimal("25.50"));
        OrderItemDomain item2 = createSampleItem(11L, new BigDecimal("14.50"));

        OrderDomain order = OrderDomain.create(paymentMethodId, statusId, outletId, List.of(item1, item2));

        assertNull(order.getId());
        assertNotNull(order.getOrderCode());
        assertEquals(new BigDecimal("40.00"), order.getTotalAmount());
        assertEquals(paymentMethodId, order.getPaymentMethodId());
        assertEquals(statusId, order.getStatusId());
        assertEquals(outletId, order.getOutletId());
        assertNotNull(order.getOrderDate());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
        assertEquals(2, order.getItems().size());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when paymentMethodId is null or zero/negative")
    void shouldThrowExceptionWhenPaymentMethodIdIsInvalid() {
        List<OrderItemDomain> items = List.of(createSampleItem(10L, new BigDecimal("10.00")));

        DomainValidationException exNull = assertThrows(DomainValidationException.class, () ->
                OrderDomain.create(null, 2L, 3L, items)
        );
        assertEquals("El método de pago es requerido", exNull.getMessage());

        DomainValidationException exZero = assertThrows(DomainValidationException.class, () ->
                OrderDomain.create(0L, 2L, 3L, items)
        );
        assertEquals("El método de pago es requerido", exZero.getMessage());

        DomainValidationException exNeg = assertThrows(DomainValidationException.class, () ->
                OrderDomain.create(-1L, 2L, 3L, items)
        );
        assertEquals("El método de pago es requerido", exNeg.getMessage());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when statusId is null or zero/negative")
    void shouldThrowExceptionWhenStatusIdIsInvalid() {
        List<OrderItemDomain> items = List.of(createSampleItem(10L, new BigDecimal("10.00")));

        DomainValidationException exNull = assertThrows(DomainValidationException.class, () ->
                OrderDomain.create(1L, null, 3L, items)
        );
        assertEquals("El estado del pedido es requerido", exNull.getMessage());

        DomainValidationException exZero = assertThrows(DomainValidationException.class, () ->
                OrderDomain.create(1L, 0L, 3L, items)
        );
        assertEquals("El estado del pedido es requerido", exZero.getMessage());

        DomainValidationException exNeg = assertThrows(DomainValidationException.class, () ->
                OrderDomain.create(1L, -2L, 3L, items)
        );
        assertEquals("El estado del pedido es requerido", exNeg.getMessage());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when outletId is null or zero/negative")
    void shouldThrowExceptionWhenOutletIdIsInvalid() {
        List<OrderItemDomain> items = List.of(createSampleItem(10L, new BigDecimal("10.00")));

        DomainValidationException exNull = assertThrows(DomainValidationException.class, () ->
                OrderDomain.create(1L, 2L, null, items)
        );
        assertEquals("El outlet es requerido", exNull.getMessage());

        DomainValidationException exZero = assertThrows(DomainValidationException.class, () ->
                OrderDomain.create(1L, 2L, 0L, items)
        );
        assertEquals("El outlet es requerido", exZero.getMessage());

        DomainValidationException exNeg = assertThrows(DomainValidationException.class, () ->
                OrderDomain.create(1L, 2L, -3L, items)
        );
        assertEquals("El outlet es requerido", exNeg.getMessage());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when items is null or empty")
    void shouldThrowExceptionWhenItemsIsInvalid() {
        DomainValidationException exNull = assertThrows(DomainValidationException.class, () ->
                OrderDomain.create(1L, 2L, 3L, null)
        );
        assertEquals("Un pedido debe contener al menos un ítem", exNull.getMessage());

        DomainValidationException exEmpty = assertThrows(DomainValidationException.class, () ->
                OrderDomain.create(1L, 2L, 3L, Collections.emptyList())
        );
        assertEquals("Un pedido debe contener al menos un ítem", exEmpty.getMessage());
    }

    @Test
    @DisplayName("Should restore OrderDomain from persistence with all fields")
    void shouldRestoreFromPersistence() {
        Long id = 100L;
        UUID orderCode = UUID.randomUUID();
        BigDecimal totalAmount = new BigDecimal("150.00");
        Long paymentMethodId = 1L;
        Long statusId = 2L;
        Long outletId = 3L;
        LocalDateTime now = LocalDateTime.now();
        OrderItemDomain item = createSampleItem(10L, totalAmount);

        OrderDomain order = OrderDomain.fromPersistence(
                id, orderCode, totalAmount, paymentMethodId, statusId, outletId, now, now, now, List.of(item)
        );

        assertEquals(id, order.getId());
        assertEquals(orderCode, order.getOrderCode());
        assertEquals(totalAmount, order.getTotalAmount());
        assertEquals(paymentMethodId, order.getPaymentMethodId());
        assertEquals(statusId, order.getStatusId());
        assertEquals(outletId, order.getOutletId());
        assertEquals(now, order.getOrderDate());
        assertEquals(now, order.getCreatedAt());
        assertEquals(now, order.getUpdatedAt());
        assertEquals(1, order.getItems().size());
    }

    @Test
    @DisplayName("Should restore OrderDomain from persistence with null items list")
    void shouldRestoreFromPersistenceWithNullItems() {
        Long id = 100L;
        UUID orderCode = UUID.randomUUID();
        BigDecimal totalAmount = BigDecimal.ZERO;
        Long paymentMethodId = 1L;
        Long statusId = 2L;
        Long outletId = 3L;
        LocalDateTime now = LocalDateTime.now();

        OrderDomain order = OrderDomain.fromPersistence(
                id, orderCode, totalAmount, paymentMethodId, statusId, outletId, now, now, now, null
        );

        assertNotNull(order.getItems());
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    @DisplayName("Should enforce unmodifiable list on getItems()")
    void shouldEnforceUnmodifiableItemsList() {
        OrderItemDomain item = createSampleItem(10L, new BigDecimal("10.00"));
        OrderDomain order = OrderDomain.create(1L, 2L, 3L, List.of(item));

        List<OrderItemDomain> items = order.getItems();
        assertThrows(UnsupportedOperationException.class, () -> items.add(item));
    }

    @Test
    @DisplayName("Should mark order as invoiced successfully")
    void shouldMarkAsInvoicedSuccessfully() {
        OrderDomain order = OrderDomain.create(1L, 2L, 3L, List.of(createSampleItem(10L, new BigDecimal("10.00"))));
        Long invoicedStatusId = 99L;

        order.markAsInvoiced(invoicedStatusId);

        assertEquals(invoicedStatusId, order.getStatusId());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when markAsInvoiced receives null or invalid statusId")
    void shouldThrowExceptionWhenMarkAsInvoicedWithInvalidStatusId() {
        OrderDomain order = OrderDomain.create(1L, 2L, 3L, List.of(createSampleItem(10L, new BigDecimal("10.00"))));

        DomainValidationException exNull = assertThrows(DomainValidationException.class, () ->
                order.markAsInvoiced(null)
        );
        assertEquals("El ID del estado de facturado es requerido", exNull.getMessage());

        DomainValidationException exZero = assertThrows(DomainValidationException.class, () ->
                order.markAsInvoiced(0L)
        );
        assertEquals("El ID del estado de facturado es requerido", exZero.getMessage());

        DomainValidationException exNeg = assertThrows(DomainValidationException.class, () ->
                order.markAsInvoiced(-5L)
        );
        assertEquals("El ID del estado de facturado es requerido", exNeg.getMessage());
    }
}
