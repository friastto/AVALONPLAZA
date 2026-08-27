package org.frias.avalon.domain.sale.domain;

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

@DisplayName("Unit Tests for ReturnDomain Entity & Aggregate Root")
class ReturnDomainTest {

    private ReturnItemDomain createSampleItem(Long id, Long productId, int quantity, String unitPriceStr, String subtotalStr) {
        return new ReturnItemDomain(
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
        @DisplayName("Should create ReturnDomain with valid parameters and calculate total refund amount")
        void testCreateReturnSuccessfully() {
            ReturnItemDomain item1 = createSampleItem(1L, 10L, 2, "1000.00", "2000.00");
            ReturnItemDomain item2 = createSampleItem(2L, 20L, 1, "3000.00", "3000.00");

            ReturnDomain returnDomain = ReturnDomain.create(
                    100L,
                    "defecto", // mixed case to test uppercase conversion
                    "Producto con falla de fábrica",
                    "reembolso", // mixed case to test uppercase conversion
                    50L,
                    5L,
                    1L,
                    20L,
                    List.of(item1, item2)
            );

            assertNull(returnDomain.getId());
            assertNotNull(returnDomain.getReturnCode());
            assertEquals(100L, returnDomain.getOriginalSaleId());
            assertEquals(new BigDecimal("5000.00"), returnDomain.getTotalRefundAmount());
            assertEquals("DEFECTO", returnDomain.getReason());
            assertEquals("Producto con falla de fábrica", returnDomain.getNotes());
            assertEquals("REEMBOLSO", returnDomain.getResolutionType());
            assertEquals(50L, returnDomain.getStatusId());
            assertEquals(5L, returnDomain.getEmployeeId());
            assertEquals(1L, returnDomain.getOutletId());
            assertEquals(20L, returnDomain.getClientId());
            assertNotNull(returnDomain.getReturnDate());
            assertNotNull(returnDomain.getCreatedAt());
            assertNotNull(returnDomain.getUpdatedAt());
            assertEquals(2, returnDomain.getItems().size());
        }

        @ParameterizedTest
        @ValueSource(strings = {"DEFECTO", "INCORRECTO", "OTRO", "defecto", "incorrecto", "otro"})
        @DisplayName("Should accept valid reason values (case insensitive)")
        void testValidReasons(String reason) {
            ReturnItemDomain item = createSampleItem(1L, 10L, 1, "500.00", "500.00");
            ReturnDomain returnDomain = ReturnDomain.create(
                    100L, reason, "Nota", "REEMBOLSO", 1L, 1L, 1L, 1L, List.of(item)
            );
            assertEquals(reason.toUpperCase(), returnDomain.getReason());
        }

        @ParameterizedTest
        @ValueSource(strings = {"REEMBOLSO", "NOTA_CREDITO", "CAMBIO", "reembolso", "nota_credito", "cambio"})
        @DisplayName("Should accept valid resolution types (case insensitive)")
        void testValidResolutionTypes(String resolutionType) {
            ReturnItemDomain item = createSampleItem(1L, 10L, 1, "500.00", "500.00");
            ReturnDomain returnDomain = ReturnDomain.create(
                    100L, "DEFECTO", "Nota", resolutionType, 1L, 1L, 1L, 1L, List.of(item)
            );
            assertEquals(resolutionType.toUpperCase(), returnDomain.getResolutionType());
        }

        @Test
        @DisplayName("Should throw DomainValidationException when originalSaleId is null")
        void testCreateReturnNullOriginalSaleId() {
            ReturnItemDomain item = createSampleItem(1L, 10L, 1, "500.00", "500.00");
            DomainValidationException ex = assertThrows(DomainValidationException.class, () ->
                    ReturnDomain.create(null, "DEFECTO", "Nota", "REEMBOLSO", 1L, 1L, 1L, 1L, List.of(item)));
            assertEquals("La venta original es requerida", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw DomainValidationException when reason is null or blank")
        void testCreateReturnNullOrBlankReason() {
            ReturnItemDomain item = createSampleItem(1L, 10L, 1, "500.00", "500.00");
            DomainValidationException exNull = assertThrows(DomainValidationException.class, () ->
                    ReturnDomain.create(100L, null, "Nota", "REEMBOLSO", 1L, 1L, 1L, 1L, List.of(item)));
            assertEquals("El motivo de devolución es requerido", exNull.getMessage());

            DomainValidationException exBlank = assertThrows(DomainValidationException.class, () ->
                    ReturnDomain.create(100L, "   ", "Nota", "REEMBOLSO", 1L, 1L, 1L, 1L, List.of(item)));
            assertEquals("El motivo de devolución es requerido", exBlank.getMessage());
        }

        @Test
        @DisplayName("Should throw DomainValidationException when resolutionType is null or blank")
        void testCreateReturnNullOrBlankResolutionType() {
            ReturnItemDomain item = createSampleItem(1L, 10L, 1, "500.00", "500.00");
            DomainValidationException exNull = assertThrows(DomainValidationException.class, () ->
                    ReturnDomain.create(100L, "DEFECTO", "Nota", null, 1L, 1L, 1L, 1L, List.of(item)));
            assertEquals("El tipo de resolución es requerido", exNull.getMessage());

            DomainValidationException exBlank = assertThrows(DomainValidationException.class, () ->
                    ReturnDomain.create(100L, "DEFECTO", "Nota", "   ", 1L, 1L, 1L, 1L, List.of(item)));
            assertEquals("El tipo de resolución es requerido", exBlank.getMessage());
        }

        @Test
        @DisplayName("Should throw DomainValidationException when items list is null or empty")
        void testCreateReturnNullOrEmptyItems() {
            DomainValidationException exNull = assertThrows(DomainValidationException.class, () ->
                    ReturnDomain.create(100L, "DEFECTO", "Nota", "REEMBOLSO", 1L, 1L, 1L, 1L, null));
            assertEquals("Una devolución debe tener al menos un ítem", exNull.getMessage());

            DomainValidationException exEmpty = assertThrows(DomainValidationException.class, () ->
                    ReturnDomain.create(100L, "DEFECTO", "Nota", "REEMBOLSO", 1L, 1L, 1L, 1L, List.of()));
            assertEquals("Una devolución debe tener al menos un ítem", exEmpty.getMessage());
        }

        @Test
        @DisplayName("Should throw DomainValidationException when reason is invalid")
        void testCreateReturnInvalidReason() {
            ReturnItemDomain item = createSampleItem(1L, 10L, 1, "500.00", "500.00");
            DomainValidationException ex = assertThrows(DomainValidationException.class, () ->
                    ReturnDomain.create(100L, "MOTIVO_INVALIDO", "Nota", "REEMBOLSO", 1L, 1L, 1L, 1L, List.of(item)));
            assertEquals("Motivo inválido. Use: DEFECTO, INCORRECTO o OTRO", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw DomainValidationException when resolutionType is invalid")
        void testCreateReturnInvalidResolutionType() {
            ReturnItemDomain item = createSampleItem(1L, 10L, 1, "500.00", "500.00");
            DomainValidationException ex = assertThrows(DomainValidationException.class, () ->
                    ReturnDomain.create(100L, "DEFECTO", "Nota", "TIPO_INVALIDO", 1L, 1L, 1L, 1L, List.of(item)));
            assertEquals("Resolución inválida. Use: REEMBOLSO, NOTA_CREDITO o CAMBIO", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Factory Method: fromPersistence")
    class FromPersistenceTests {

        @Test
        @DisplayName("Should restore ReturnDomain from persistence correctly")
        void testFromPersistence() {
            UUID code = UUID.randomUUID();
            LocalDateTime returnDate = LocalDateTime.now().minusHours(2);
            LocalDateTime createdAt = LocalDateTime.now().minusHours(2);
            LocalDateTime updatedAt = LocalDateTime.now();
            ReturnItemDomain item = createSampleItem(1L, 10L, 1, "500.00", "500.00");

            ReturnDomain returnDomain = ReturnDomain.fromPersistence(
                    50L, code, 100L, new BigDecimal("500.00"), "DEFECTO", "Sin notas",
                    "CAMBIO", 10L, 5L, 2L, 30L, returnDate, createdAt, updatedAt, List.of(item)
            );

            assertEquals(50L, returnDomain.getId());
            assertEquals(code, returnDomain.getReturnCode());
            assertEquals(100L, returnDomain.getOriginalSaleId());
            assertEquals(new BigDecimal("500.00"), returnDomain.getTotalRefundAmount());
            assertEquals("DEFECTO", returnDomain.getReason());
            assertEquals("Sin notas", returnDomain.getNotes());
            assertEquals("CAMBIO", returnDomain.getResolutionType());
            assertEquals(10L, returnDomain.getStatusId());
            assertEquals(5L, returnDomain.getEmployeeId());
            assertEquals(2L, returnDomain.getOutletId());
            assertEquals(30L, returnDomain.getClientId());
            assertEquals(returnDate, returnDomain.getReturnDate());
            assertEquals(createdAt, returnDomain.getCreatedAt());
            assertEquals(updatedAt, returnDomain.getUpdatedAt());
            assertEquals(1, returnDomain.getItems().size());
            assertEquals(item, returnDomain.getItems().get(0));
        }

        @Test
        @DisplayName("Should restore ReturnDomain from persistence with null items list as empty list")
        void testFromPersistenceWithNullItems() {
            UUID code = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            ReturnDomain returnDomain = ReturnDomain.fromPersistence(
                    50L, code, 100L, BigDecimal.ZERO, "DEFECTO", null,
                    "CAMBIO", 10L, 5L, 2L, 30L, now, now, now, null
            );

            assertNotNull(returnDomain.getItems());
            assertTrue(returnDomain.getItems().isEmpty());
        }
    }

    @Nested
    @DisplayName("Immutability Invariants")
    class ImmutabilityTests {

        @Test
        @DisplayName("Enforce immutability invariants on items list")
        void testImmutabilityInvariants() {
            ReturnItemDomain item = createSampleItem(1L, 10L, 1, "500.00", "500.00");
            List<ReturnItemDomain> mutableList = new ArrayList<>();
            mutableList.add(item);

            ReturnDomain returnDomain = ReturnDomain.create(
                    100L, "DEFECTO", "Nota", "CAMBIO", 1L, 1L, 1L, 1L, mutableList
            );

            // Modifying input list should not affect internal list
            mutableList.clear();
            assertEquals(1, returnDomain.getItems().size());

            // Returned list must be unmodifiable
            List<ReturnItemDomain> items = returnDomain.getItems();
            assertThrows(UnsupportedOperationException.class, () -> items.add(item));
            assertThrows(UnsupportedOperationException.class, () -> items.remove(0));
        }
    }
}
