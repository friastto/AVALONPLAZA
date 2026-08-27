package org.frias.avalon.domain.sale.domain;

import org.frias.avalon.core.exeptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for OrderItemDomain")
class OrderItemDomainTest {

    @Test
    @DisplayName("Should create OrderItemDomain successfully with valid data")
    void shouldCreateOrderItemDomainSuccessfully() {
        Long id = 1L;
        Long productId = 10L;
        Integer quantityInBaseUnits = 5000;
        String displayQuantity = "5.000 KG";
        BigDecimal unitPrice = new BigDecimal("12.50");
        BigDecimal subtotal = new BigDecimal("62.50");
        Long unitMeasureId = 2L;

        OrderItemDomain item = new OrderItemDomain(id, productId, quantityInBaseUnits, displayQuantity, unitPrice, subtotal, unitMeasureId);

        assertEquals(id, item.getId());
        assertEquals(productId, item.getProductId());
        assertEquals(quantityInBaseUnits, item.getQuantityInBaseUnits());
        assertEquals(displayQuantity, item.getDisplayQuantity());
        assertEquals(unitPrice, item.getUnitPrice());
        assertEquals(subtotal, item.getSubtotal());
        assertEquals(unitMeasureId, item.getUnitMeasureId());
    }

    @Test
    @DisplayName("Should create OrderItemDomain with null id when not yet persisted")
    void shouldCreateOrderItemDomainWithNullId() {
        OrderItemDomain item = new OrderItemDomain(null, 10L, 2, "2 UND", BigDecimal.ZERO, BigDecimal.ZERO, 3L);

        assertNull(item.getId());
        assertEquals(10L, item.getProductId());
        assertEquals(2, item.getQuantityInBaseUnits());
        assertEquals("2 UND", item.getDisplayQuantity());
        assertEquals(BigDecimal.ZERO, item.getUnitPrice());
        assertEquals(BigDecimal.ZERO, item.getSubtotal());
        assertEquals(3L, item.getUnitMeasureId());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when productId is null")
    void shouldThrowExceptionWhenProductIdIsNull() {
        DomainValidationException ex = assertThrows(DomainValidationException.class, () ->
                new OrderItemDomain(1L, null, 10, "10", new BigDecimal("5.00"), new BigDecimal("50.00"), 1L)
        );
        assertEquals("El ID de producto es requerido", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when productId is zero or negative")
    void shouldThrowExceptionWhenProductIdIsZeroOrNegative() {
        DomainValidationException ex1 = assertThrows(DomainValidationException.class, () ->
                new OrderItemDomain(1L, 0L, 10, "10", new BigDecimal("5.00"), new BigDecimal("50.00"), 1L)
        );
        assertEquals("El ID de producto es requerido", ex1.getMessage());

        DomainValidationException ex2 = assertThrows(DomainValidationException.class, () ->
                new OrderItemDomain(1L, -5L, 10, "10", new BigDecimal("5.00"), new BigDecimal("50.00"), 1L)
        );
        assertEquals("El ID de producto es requerido", ex2.getMessage());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when quantityInBaseUnits is null")
    void shouldThrowExceptionWhenQuantityIsNull() {
        DomainValidationException ex = assertThrows(DomainValidationException.class, () ->
                new OrderItemDomain(1L, 10L, null, "10", new BigDecimal("5.00"), new BigDecimal("50.00"), 1L)
        );
        assertEquals("La cantidad debe ser mayor a cero", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when quantityInBaseUnits is zero or negative")
    void shouldThrowExceptionWhenQuantityIsZeroOrNegative() {
        DomainValidationException ex1 = assertThrows(DomainValidationException.class, () ->
                new OrderItemDomain(1L, 10L, 0, "0", new BigDecimal("5.00"), new BigDecimal("50.00"), 1L)
        );
        assertEquals("La cantidad debe ser mayor a cero", ex1.getMessage());

        DomainValidationException ex2 = assertThrows(DomainValidationException.class, () ->
                new OrderItemDomain(1L, 10L, -1, "-1", new BigDecimal("5.00"), new BigDecimal("50.00"), 1L)
        );
        assertEquals("La cantidad debe ser mayor a cero", ex2.getMessage());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when unitPrice is null")
    void shouldThrowExceptionWhenUnitPriceIsNull() {
        DomainValidationException ex = assertThrows(DomainValidationException.class, () ->
                new OrderItemDomain(1L, 10L, 5, "5", null, new BigDecimal("50.00"), 1L)
        );
        assertEquals("El precio unitario no puede ser negativo", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when unitPrice is negative")
    void shouldThrowExceptionWhenUnitPriceIsNegative() {
        DomainValidationException ex = assertThrows(DomainValidationException.class, () ->
                new OrderItemDomain(1L, 10L, 5, "5", new BigDecimal("-0.01"), new BigDecimal("50.00"), 1L)
        );
        assertEquals("El precio unitario no puede ser negativo", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when subtotal is null")
    void shouldThrowExceptionWhenSubtotalIsNull() {
        DomainValidationException ex = assertThrows(DomainValidationException.class, () ->
                new OrderItemDomain(1L, 10L, 5, "5", new BigDecimal("10.00"), null, 1L)
        );
        assertEquals("El subtotal no puede ser negativo", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when subtotal is negative")
    void shouldThrowExceptionWhenSubtotalIsNegative() {
        DomainValidationException ex = assertThrows(DomainValidationException.class, () ->
                new OrderItemDomain(1L, 10L, 5, "5", new BigDecimal("10.00"), new BigDecimal("-1.00"), 1L)
        );
        assertEquals("El subtotal no puede ser negativo", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when unitMeasureId is null")
    void shouldThrowExceptionWhenUnitMeasureIdIsNull() {
        DomainValidationException ex = assertThrows(DomainValidationException.class, () ->
                new OrderItemDomain(1L, 10L, 5, "5", new BigDecimal("10.00"), new BigDecimal("50.00"), null)
        );
        assertEquals("La unidad de medida es requerida", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when unitMeasureId is zero or negative")
    void shouldThrowExceptionWhenUnitMeasureIdIsZeroOrNegative() {
        DomainValidationException ex1 = assertThrows(DomainValidationException.class, () ->
                new OrderItemDomain(1L, 10L, 5, "5", new BigDecimal("10.00"), new BigDecimal("50.00"), 0L)
        );
        assertEquals("La unidad de medida es requerida", ex1.getMessage());

        DomainValidationException ex2 = assertThrows(DomainValidationException.class, () ->
                new OrderItemDomain(1L, 10L, 5, "5", new BigDecimal("10.00"), new BigDecimal("50.00"), -3L)
        );
        assertEquals("La unidad de medida es requerida", ex2.getMessage());
    }
}
