package org.frias.avalon.domain.sale.domain;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for SaleDomain Entity & Aggregate Root")
class SaleDomainTest {

    private SaleItemDomain createSampleItem(Long id, Long productId, int quantity, String unitPriceStr, String subtotalStr) {
        return new SaleItemDomain(
                id,
                productId,
                quantity,
                quantity + " UND",
                new BigDecimal(unitPriceStr),
                new BigDecimal(subtotalStr),
                1L
        );
    }

    @Nested
    @DisplayName("Factory Method: create")
    class CreateTests {

        @Test
        @DisplayName("Should create SaleDomain with valid parameters and calculate total amount correctly")
        void testCreateSaleSuccessfully() {
            SaleItemDomain item1 = createSampleItem(1L, 10L, 5, "100.00", "500.00");
            SaleItemDomain item2 = createSampleItem(2L, 11L, 2, "250.00", "500.00");

            SaleDomain sale = SaleDomain.create(1L, 2L, 3L, 4L, 5L, List.of(item1, item2));

            assertNull(sale.getId());
            assertNotNull(sale.getSaleCode());
            assertEquals(new BigDecimal("1000.00"), sale.getTotalAmount());
            assertEquals(BigDecimal.ZERO, sale.getAmountReceived());
            assertEquals(BigDecimal.ZERO, sale.getChangeGiven());
            assertEquals(1L, sale.getPaymentMethodId());
            assertEquals(2L, sale.getStatusId());
            assertEquals(3L, sale.getClientId());
            assertEquals(4L, sale.getOutletId());
            assertEquals(5L, sale.getEmployeeId());
            assertNotNull(sale.getSaleDate());
            assertNotNull(sale.getCreatedAt());
            assertNotNull(sale.getUpdatedAt());
            assertEquals(2, sale.getItems().size());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L})
        @DisplayName("Should throw DomainValidationException when paymentMethodId is zero or negative")
        void testCreateSaleInvalidPaymentMethodIdValues(Long invalidId) {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "100.00", "100.00");
            assertThrows(DomainValidationException.class, () -> SaleDomain.create(invalidId, 2L, 3L, 4L, 5L, List.of(item)));
        }

        @Test
        @DisplayName("Should throw DomainValidationException when paymentMethodId is null")
        void testCreateSaleNullPaymentMethodId() {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "100.00", "100.00");
            DomainValidationException ex = assertThrows(DomainValidationException.class,
                    () -> SaleDomain.create(null, 2L, 3L, 4L, 5L, List.of(item)));
            assertEquals("El método de pago es requerido", ex.getMessage());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -5L})
        @DisplayName("Should throw DomainValidationException when statusId is zero or negative")
        void testCreateSaleInvalidStatusIdValues(Long invalidId) {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "100.00", "100.00");
            assertThrows(DomainValidationException.class, () -> SaleDomain.create(1L, invalidId, 3L, 4L, 5L, List.of(item)));
        }

        @Test
        @DisplayName("Should throw DomainValidationException when statusId is null")
        void testCreateSaleNullStatusId() {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "100.00", "100.00");
            DomainValidationException ex = assertThrows(DomainValidationException.class,
                    () -> SaleDomain.create(1L, null, 3L, 4L, 5L, List.of(item)));
            assertEquals("El estado de la venta es requerido", ex.getMessage());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -99L})
        @DisplayName("Should throw DomainValidationException when clientId is zero or negative")
        void testCreateSaleInvalidClientIdValues(Long invalidId) {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "100.00", "100.00");
            assertThrows(DomainValidationException.class, () -> SaleDomain.create(1L, 2L, invalidId, 4L, 5L, List.of(item)));
        }

        @Test
        @DisplayName("Should throw DomainValidationException when clientId is null")
        void testCreateSaleNullClientId() {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "100.00", "100.00");
            DomainValidationException ex = assertThrows(DomainValidationException.class,
                    () -> SaleDomain.create(1L, 2L, null, 4L, 5L, List.of(item)));
            assertEquals("El cliente es requerido", ex.getMessage());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -20L})
        @DisplayName("Should throw DomainValidationException when outletId is zero or negative")
        void testCreateSaleInvalidOutletIdValues(Long invalidId) {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "100.00", "100.00");
            assertThrows(DomainValidationException.class, () -> SaleDomain.create(1L, 2L, 3L, invalidId, 5L, List.of(item)));
        }

        @Test
        @DisplayName("Should throw DomainValidationException when outletId is null")
        void testCreateSaleNullOutletId() {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "100.00", "100.00");
            DomainValidationException ex = assertThrows(DomainValidationException.class,
                    () -> SaleDomain.create(1L, 2L, 3L, null, 5L, List.of(item)));
            assertEquals("El outlet es requerido", ex.getMessage());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -50L})
        @DisplayName("Should throw DomainValidationException when employeeId is zero or negative")
        void testCreateSaleInvalidEmployeeIdValues(Long invalidId) {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "100.00", "100.00");
            assertThrows(DomainValidationException.class, () -> SaleDomain.create(1L, 2L, 3L, 4L, invalidId, List.of(item)));
        }

        @Test
        @DisplayName("Should throw DomainValidationException when employeeId is null")
        void testCreateSaleNullEmployeeId() {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "100.00", "100.00");
            DomainValidationException ex = assertThrows(DomainValidationException.class,
                    () -> SaleDomain.create(1L, 2L, 3L, 4L, null, List.of(item)));
            assertEquals("El empleado es requerido", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw DomainValidationException when items list is null or empty")
        void testCreateSaleWithoutItemsThrowsException() {
            DomainValidationException ex1 = assertThrows(DomainValidationException.class,
                    () -> SaleDomain.create(1L, 2L, 3L, 4L, 5L, null));
            assertEquals("Una venta debe contener al menos un ítem", ex1.getMessage());

            DomainValidationException ex2 = assertThrows(DomainValidationException.class,
                    () -> SaleDomain.create(1L, 2L, 3L, 4L, 5L, List.of()));
            assertEquals("Una venta debe contener al menos un ítem", ex2.getMessage());
        }
    }

    @Nested
    @DisplayName("Factory Method: fromPersistence")
    class FromPersistenceTests {

        @Test
        @DisplayName("Should restore SaleDomain from persistence correctly with all fields")
        void testFromPersistence() {
            UUID code = UUID.randomUUID();
            LocalDateTime saleDate = LocalDateTime.now().minusDays(1);
            LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
            LocalDateTime updatedAt = LocalDateTime.now();
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "150.00", "150.00");

            SaleDomain sale = SaleDomain.fromPersistence(
                    100L, code, new BigDecimal("150.00"), new BigDecimal("200.00"), new BigDecimal("50.00"),
                    1L, 2L, 3L, 4L, 5L, saleDate, createdAt, updatedAt, List.of(item)
            );

            assertEquals(100L, sale.getId());
            assertEquals(code, sale.getSaleCode());
            assertEquals(new BigDecimal("150.00"), sale.getTotalAmount());
            assertEquals(new BigDecimal("200.00"), sale.getAmountReceived());
            assertEquals(new BigDecimal("50.00"), sale.getChangeGiven());
            assertEquals(1L, sale.getPaymentMethodId());
            assertEquals(2L, sale.getStatusId());
            assertEquals(3L, sale.getClientId());
            assertEquals(4L, sale.getOutletId());
            assertEquals(5L, sale.getEmployeeId());
            assertEquals(saleDate, sale.getSaleDate());
            assertEquals(createdAt, sale.getCreatedAt());
            assertEquals(updatedAt, sale.getUpdatedAt());
            assertEquals(1, sale.getItems().size());
            assertEquals(item, sale.getItems().get(0));
        }

        @Test
        @DisplayName("Should restore SaleDomain from persistence with null items list as empty list")
        void testFromPersistenceWithNullItems() {
            UUID code = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            SaleDomain sale = SaleDomain.fromPersistence(
                    100L, code, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    1L, 2L, 3L, 4L, 5L, now, now, now, null
            );

            assertNotNull(sale.getItems());
            assertTrue(sale.getItems().isEmpty());
        }
    }

    @Nested
    @DisplayName("Payment Application: applyPayment")
    class ApplyPaymentTests {

        @Test
        @DisplayName("Should apply standard payment successfully and calculate correct change")
        void testApplyPaymentSuccessfullyAndCalculatesChangeCorrectly() {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "150.00", "150.00");
            SaleDomain sale = SaleDomain.create(1L, 2L, 3L, 4L, 5L, List.of(item));

            sale.applyPayment(new BigDecimal("200.00"));

            assertEquals(new BigDecimal("200.00"), sale.getAmountReceived());
            assertEquals(new BigDecimal("50.00"), sale.getChangeGiven());
        }

        @Test
        @DisplayName("Should apply exact payment and set change to zero")
        void testApplyPaymentExactAmount() {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "150.00", "150.00");
            SaleDomain sale = SaleDomain.create(1L, 2L, 3L, 4L, 5L, List.of(item));

            sale.applyPayment(new BigDecimal("150.00"));

            assertEquals(new BigDecimal("150.00"), sale.getAmountReceived());
            assertEquals(0, sale.getChangeGiven().compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Should throw BusinessException when amountReceived is null")
        void testApplyPaymentNullThrowsException() {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "150.00", "150.00");
            SaleDomain sale = SaleDomain.create(1L, 2L, 3L, 4L, 5L, List.of(item));

            BusinessException ex = assertThrows(BusinessException.class, () -> sale.applyPayment(null));
            assertEquals("El monto recibido no puede ser negativo o nulo", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw BusinessException when amountReceived is negative")
        void testApplyPaymentNegativeThrowsException() {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "150.00", "150.00");
            SaleDomain sale = SaleDomain.create(1L, 2L, 3L, 4L, 5L, List.of(item));

            BusinessException ex = assertThrows(BusinessException.class, () -> sale.applyPayment(new BigDecimal("-10.00")));
            assertEquals("El monto recibido no puede ser negativo o nulo", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw BusinessException when standard payment amount is less than total")
        void testApplyPaymentInsufficientThrowsException() {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "150.00", "150.00");
            SaleDomain sale = SaleDomain.create(1L, 2L, 3L, 4L, 5L, List.of(item));

            BusinessException ex = assertThrows(BusinessException.class, () -> sale.applyPayment(new BigDecimal("100.00")));
            assertTrue(ex.getMessage().contains("es menor que el valor total a pagar"));
        }

        @Test
        @DisplayName("Should apply fiado payment without enforcing minimum amount and setting change to zero")
        void testApplyPaymentFiado() {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "150.00", "150.00");
            SaleDomain sale = SaleDomain.create(1L, 2L, 3L, 4L, 5L, List.of(item));

            sale.applyPayment(BigDecimal.ZERO, true);

            assertEquals(BigDecimal.ZERO, sale.getAmountReceived());
            assertEquals(BigDecimal.ZERO, sale.getChangeGiven());
        }

        @Test
        @DisplayName("Should throw BusinessException when fiado payment is negative or null")
        void testApplyPaymentFiadoNegativeOrNullThrowsException() {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "150.00", "150.00");
            SaleDomain sale = SaleDomain.create(1L, 2L, 3L, 4L, 5L, List.of(item));

            assertThrows(BusinessException.class, () -> sale.applyPayment(null, true));
            assertThrows(BusinessException.class, () -> sale.applyPayment(new BigDecimal("-5.00"), true));
        }
    }

    @Nested
    @DisplayName("Immutability Invariants")
    class ImmutabilityTests {

        @Test
        @DisplayName("Enforce immutability invariants on items list")
        void testImmutabilityInvariants() {
            SaleItemDomain item = createSampleItem(1L, 10L, 1, "100.00", "100.00");
            List<SaleItemDomain> mutableList = new ArrayList<>();
            mutableList.add(item);

            SaleDomain sale = SaleDomain.create(1L, 2L, 3L, 4L, 5L, mutableList);

            // Modifying input list should not affect internal list
            mutableList.clear();
            assertEquals(1, sale.getItems().size());

            // Returned list must be unmodifiable
            List<SaleItemDomain> items = sale.getItems();
            assertThrows(UnsupportedOperationException.class, () -> items.add(item));
            assertThrows(UnsupportedOperationException.class, () -> items.remove(0));
        }
    }
}
