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
 * Pruebas unitarias para ReturnEntity.
 */
@DisplayName("Unit Tests for ReturnEntity")
class ReturnEntityTest {

    @Test
    @DisplayName("NoArgsConstructor and Getters/Setters should set and retrieve all properties correctly")
    void noArgsConstructorAndGettersSetters_WorkCorrectly() {
        ReturnEntity entity = new ReturnEntity();

        Long id = 1L;
        UUID returnCode = UUID.randomUUID();
        Long originalSaleId = 100L;
        BigDecimal totalRefundAmount = new BigDecimal("150.00");
        String reason = "DEFECTO";
        String notes = "Empaque roto";
        String resolutionType = "REEMBOLSO";
        Long statusId = 2L;
        Long employeeId = 3L;
        Long outletId = 4L;
        Long clientId = 5L;
        LocalDateTime returnDate = LocalDateTime.now();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        List<ReturnItemEntity> items = new ArrayList<>();

        entity.setId(id);
        entity.setReturnCode(returnCode);
        entity.setOriginalSaleId(originalSaleId);
        entity.setTotalRefundAmount(totalRefundAmount);
        entity.setReason(reason);
        entity.setNotes(notes);
        entity.setResolutionType(resolutionType);
        entity.setStatusId(statusId);
        entity.setEmployeeId(employeeId);
        entity.setOutletId(outletId);
        entity.setClientId(clientId);
        entity.setReturnDate(returnDate);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        entity.setItems(items);

        assertEquals(id, entity.getId());
        assertEquals(returnCode, entity.getReturnCode());
        assertEquals(originalSaleId, entity.getOriginalSaleId());
        assertEquals(totalRefundAmount, entity.getTotalRefundAmount());
        assertEquals(reason, entity.getReason());
        assertEquals(notes, entity.getNotes());
        assertEquals(resolutionType, entity.getResolutionType());
        assertEquals(statusId, entity.getStatusId());
        assertEquals(employeeId, entity.getEmployeeId());
        assertEquals(outletId, entity.getOutletId());
        assertEquals(clientId, entity.getClientId());
        assertEquals(returnDate, entity.getReturnDate());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
        assertEquals(items, entity.getItems());
    }

    @Test
    @DisplayName("AllArgsConstructor and Builder should initialize all fields correctly")
    void allArgsConstructorAndBuilder_WorkCorrectly() {
        Long id = 10L;
        UUID returnCode = UUID.randomUUID();
        Long originalSaleId = 200L;
        BigDecimal totalRefundAmount = new BigDecimal("500.00");
        String reason = "INCORRECTO";
        String notes = "Producto equivocado";
        String resolutionType = "CAMBIO";
        Long statusId = 1L;
        Long employeeId = 2L;
        Long outletId = 3L;
        Long clientId = 4L;
        LocalDateTime returnDate = LocalDateTime.now();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        List<ReturnItemEntity> items = List.of(new ReturnItemEntity());

        ReturnEntity entityFromBuilder = ReturnEntity.builder()
                .id(id)
                .returnCode(returnCode)
                .originalSaleId(originalSaleId)
                .totalRefundAmount(totalRefundAmount)
                .reason(reason)
                .notes(notes)
                .resolutionType(resolutionType)
                .statusId(statusId)
                .employeeId(employeeId)
                .outletId(outletId)
                .clientId(clientId)
                .returnDate(returnDate)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .items(items)
                .build();

        assertEquals(id, entityFromBuilder.getId());
        assertEquals(returnCode, entityFromBuilder.getReturnCode());
        assertEquals(originalSaleId, entityFromBuilder.getOriginalSaleId());
        assertEquals(totalRefundAmount, entityFromBuilder.getTotalRefundAmount());
        assertEquals(reason, entityFromBuilder.getReason());
        assertEquals(notes, entityFromBuilder.getNotes());
        assertEquals(resolutionType, entityFromBuilder.getResolutionType());
        assertEquals(statusId, entityFromBuilder.getStatusId());
        assertEquals(employeeId, entityFromBuilder.getEmployeeId());
        assertEquals(outletId, entityFromBuilder.getOutletId());
        assertEquals(clientId, entityFromBuilder.getClientId());
        assertEquals(returnDate, entityFromBuilder.getReturnDate());
        assertEquals(createdAt, entityFromBuilder.getCreatedAt());
        assertEquals(updatedAt, entityFromBuilder.getUpdatedAt());
        assertEquals(items, entityFromBuilder.getItems());

        ReturnEntity entityFromAllArgs = new ReturnEntity(
                id, returnCode, originalSaleId, totalRefundAmount, reason, notes, resolutionType,
                statusId, employeeId, outletId, clientId, returnDate, createdAt, updatedAt, items
        );

        assertEquals(id, entityFromAllArgs.getId());
        assertEquals(returnCode, entityFromAllArgs.getReturnCode());
    }

    @Test
    @DisplayName("onCreate should initialize returnCode, returnDate, createdAt, and updatedAt when fields are null")
    void onCreate_WhenFieldsAreNull_PopulatesDefaultCodeDateAndTimestamps() {
        ReturnEntity entity = new ReturnEntity();

        assertNull(entity.getReturnCode());
        assertNull(entity.getReturnDate());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());

        entity.onCreate();

        assertNotNull(entity.getReturnCode());
        assertNotNull(entity.getReturnDate());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("onCreate should preserve existing returnCode and returnDate if they are already present")
    void onCreate_WhenFieldsAreAlreadySet_PreservesExistingCodeAndDate() {
        UUID existingCode = UUID.randomUUID();
        LocalDateTime existingReturnDate = LocalDateTime.now().minusDays(3);

        ReturnEntity entity = ReturnEntity.builder()
                .returnCode(existingCode)
                .returnDate(existingReturnDate)
                .build();

        entity.onCreate();

        assertEquals(existingCode, entity.getReturnCode());
        assertEquals(existingReturnDate, entity.getReturnDate());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("onUpdate should refresh updatedAt timestamp")
    void onUpdate_UpdatesUpdatedAtTimestamp() {
        ReturnEntity entity = new ReturnEntity();
        LocalDateTime past = LocalDateTime.now().minusHours(2);
        entity.setUpdatedAt(past);

        entity.onUpdate();

        assertNotNull(entity.getUpdatedAt());
        assertTrue(entity.getUpdatedAt().isAfter(past) || entity.getUpdatedAt().isEqual(past));
    }
}
