package org.frias.avalon.domain.sale.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for ReturnItemDomain Entity")
class ReturnItemDomainTest {

    @Nested
    @DisplayName("Constructor & Getter Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create ReturnItemDomain with valid parameters and getters return correct values")
        void testCreateReturnItemSuccessfully() {
            ReturnItemDomain item = new ReturnItemDomain(
                    5L,
                    50L,
                    3,
                    "3 UND",
                    new BigDecimal("1500.00"),
                    new BigDecimal("4500.00"),
                    2L
            );

            assertEquals(5L, item.getId());
            assertEquals(50L, item.getProductId());
            assertEquals(3, item.getQuantityInBaseUnits());
            assertEquals("3 UND", item.getDisplayQuantity());
            assertEquals(new BigDecimal("1500.00"), item.getUnitPrice());
            assertEquals(new BigDecimal("4500.00"), item.getSubtotal());
            assertEquals(2L, item.getUnitMeasureId());
        }

        @Test
        @DisplayName("Should create ReturnItemDomain with null id prior to persistence")
        void testCreateReturnItemWithNullId() {
            ReturnItemDomain item = new ReturnItemDomain(
                    null,
                    50L,
                    1000,
                    "1 KG",
                    new BigDecimal("8000.00"),
                    new BigDecimal("8000.00"),
                    3L
            );

            assertNull(item.getId());
            assertEquals(50L, item.getProductId());
            assertEquals(1000, item.getQuantityInBaseUnits());
            assertEquals("1 KG", item.getDisplayQuantity());
            assertEquals(new BigDecimal("8000.00"), item.getUnitPrice());
            assertEquals(new BigDecimal("8000.00"), item.getSubtotal());
            assertEquals(3L, item.getUnitMeasureId());
        }

        @Test
        @DisplayName("Should verify values for weighable return item")
        void testWeighableReturnItem() {
            ReturnItemDomain item = new ReturnItemDomain(
                    15L,
                    102L,
                    2500, // 2.5 KG in grams
                    "2.500 KG",
                    new BigDecimal("12000.00"),
                    new BigDecimal("30000.00"),
                    5L
            );

            assertEquals(15L, item.getId());
            assertEquals(102L, item.getProductId());
            assertEquals(2500, item.getQuantityInBaseUnits());
            assertEquals("2.500 KG", item.getDisplayQuantity());
            assertEquals(new BigDecimal("12000.00"), item.getUnitPrice());
            assertEquals(new BigDecimal("30000.00"), item.getSubtotal());
            assertEquals(5L, item.getUnitMeasureId());
        }
    }
}
