package org.frias.avalon.domain.sale.domain;

import org.frias.avalon.core.exeptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for SaleItemDomain Entity")
class SaleItemDomainTest {

    @Nested
    @DisplayName("Constructor & Validation Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create SaleItemDomain with valid parameters")
        void testCreateSaleItemSuccessfully() {
            SaleItemDomain item = new SaleItemDomain(
                    10L,
                    100L,
                    5,
                    "5 UND",
                    new BigDecimal("2500.00"),
                    new BigDecimal("12500.00"),
                    1L
            );

            assertEquals(10L, item.getId());
            assertEquals(100L, item.getProductId());
            assertEquals(5, item.getQuantityInBaseUnits());
            assertEquals("5 UND", item.getDisplayQuantity());
            assertEquals(new BigDecimal("2500.00"), item.getUnitPrice());
            assertEquals(new BigDecimal("12500.00"), item.getSubtotal());
            assertEquals(1L, item.getUnitMeasureId());
        }

        @Test
        @DisplayName("Should create SaleItemDomain with null id prior to persistence")
        void testCreateSaleItemWithNullId() {
            SaleItemDomain item = new SaleItemDomain(
                    null,
                    100L,
                    1,
                    "1.5 KG",
                    new BigDecimal("1000.00"),
                    new BigDecimal("1500.00"),
                    2L
            );

            assertNull(item.getId());
            assertEquals(100L, item.getProductId());
            assertEquals(1, item.getQuantityInBaseUnits());
            assertEquals("1.5 KG", item.getDisplayQuantity());
            assertEquals(new BigDecimal("1000.00"), item.getUnitPrice());
            assertEquals(new BigDecimal("1500.00"), item.getSubtotal());
            assertEquals(2L, item.getUnitMeasureId());
        }

        @Test
        @DisplayName("Should throw DomainValidationException when productId is null")
        void testNullProductId() {
            DomainValidationException ex = assertThrows(DomainValidationException.class, () ->
                    new SaleItemDomain(1L, null, 5, "5 UND", new BigDecimal("100.00"), new BigDecimal("500.00"), 1L));
            assertEquals("El ID de producto es requerido", ex.getMessage());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -99L})
        @DisplayName("Should throw DomainValidationException when productId is zero or negative")
        void testNonPositiveProductId(Long invalidProductId) {
            assertThrows(DomainValidationException.class, () ->
                    new SaleItemDomain(1L, invalidProductId, 5, "5 UND", new BigDecimal("100.00"), new BigDecimal("500.00"), 1L));
        }

        @Test
        @DisplayName("Should throw DomainValidationException when quantityInBaseUnits is null")
        void testNullQuantityInBaseUnits() {
            DomainValidationException ex = assertThrows(DomainValidationException.class, () ->
                    new SaleItemDomain(1L, 100L, null, "5 UND", new BigDecimal("100.00"), new BigDecimal("500.00"), 1L));
            assertEquals("La cantidad debe ser mayor a cero", ex.getMessage());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -50})
        @DisplayName("Should throw DomainValidationException when quantityInBaseUnits is zero or negative")
        void testNonPositiveQuantityInBaseUnits(int invalidQty) {
            assertThrows(DomainValidationException.class, () ->
                    new SaleItemDomain(1L, 100L, invalidQty, "0 UND", new BigDecimal("100.00"), new BigDecimal("0.00"), 1L));
        }

        @Test
        @DisplayName("Should throw DomainValidationException when unitPrice is null or negative")
        void testInvalidUnitPrice() {
            DomainValidationException exNull = assertThrows(DomainValidationException.class, () ->
                    new SaleItemDomain(1L, 100L, 5, "5 UND", null, new BigDecimal("500.00"), 1L));
            assertEquals("El precio unitario no puede ser negativo", exNull.getMessage());

            DomainValidationException exNeg = assertThrows(DomainValidationException.class, () ->
                    new SaleItemDomain(1L, 100L, 5, "5 UND", new BigDecimal("-10.00"), new BigDecimal("500.00"), 1L));
            assertEquals("El precio unitario no puede ser negativo", exNeg.getMessage());

            // Zero price is valid (free item or promotional sample)
            assertDoesNotThrow(() ->
                    new SaleItemDomain(1L, 100L, 5, "5 UND", BigDecimal.ZERO, BigDecimal.ZERO, 1L));
        }

        @Test
        @DisplayName("Should throw DomainValidationException when subtotal is null or negative")
        void testInvalidSubtotal() {
            DomainValidationException exNull = assertThrows(DomainValidationException.class, () ->
                    new SaleItemDomain(1L, 100L, 5, "5 UND", new BigDecimal("100.00"), null, 1L));
            assertEquals("El subtotal no puede ser negativo", exNull.getMessage());

            DomainValidationException exNeg = assertThrows(DomainValidationException.class, () ->
                    new SaleItemDomain(1L, 100L, 5, "5 UND", new BigDecimal("100.00"), new BigDecimal("-50.00"), 1L));
            assertEquals("El subtotal no puede ser negativo", exNeg.getMessage());

            // Zero subtotal is valid
            assertDoesNotThrow(() ->
                    new SaleItemDomain(1L, 100L, 5, "5 UND", BigDecimal.ZERO, BigDecimal.ZERO, 1L));
        }

        @Test
        @DisplayName("Should throw DomainValidationException when unitMeasureId is null")
        void testNullUnitMeasureId() {
            DomainValidationException ex = assertThrows(DomainValidationException.class, () ->
                    new SaleItemDomain(1L, 100L, 5, "5 UND", new BigDecimal("100.00"), new BigDecimal("500.00"), null));
            assertEquals("La unidad de medida es requerida", ex.getMessage());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -10L})
        @DisplayName("Should throw DomainValidationException when unitMeasureId is zero or negative")
        void testNonPositiveUnitMeasureId(Long invalidUnitMeasureId) {
            assertThrows(DomainValidationException.class, () ->
                    new SaleItemDomain(1L, 100L, 5, "5 UND", new BigDecimal("100.00"), new BigDecimal("500.00"), invalidUnitMeasureId));
        }
    }
}
