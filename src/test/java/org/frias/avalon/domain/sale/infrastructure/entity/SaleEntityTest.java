package org.frias.avalon.domain.sale.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for SaleEntity and SaleItemEntity JPA Mappings")
class SaleEntityTest {

    @Test
    @DisplayName("onCreate pre-persist callback should populate createdAt, updatedAt, saleCode, and saleDate if null")
    void onCreate_WhenFieldsAreNull_PopulatesDefaultValues() throws Exception {
        SaleEntity entity = SaleEntity.builder()
                .totalAmount(new BigDecimal("100.00"))
                .amountReceived(new BigDecimal("100.00"))
                .changeGiven(BigDecimal.ZERO)
                .paymentMethodId(1L)
                .statusId(1L)
                .clientId(2L)
                .outletId(3L)
                .employeeId(4L)
                .build();

        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
        assertNull(entity.getSaleCode());
        assertNull(entity.getSaleDate());

        Method onCreateMethod = SaleEntity.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        onCreateMethod.invoke(entity);

        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        assertNotNull(entity.getSaleCode());
        assertNotNull(entity.getSaleDate());
    }

    @Test
    @DisplayName("onCreate pre-persist callback should preserve existing saleCode and saleDate if already set")
    void onCreate_WhenFieldsAreSet_PreservesExistingValues() throws Exception {
        UUID customCode = UUID.randomUUID();
        LocalDateTime customDate = LocalDateTime.now().minusDays(5);

        SaleEntity entity = SaleEntity.builder()
                .saleCode(customCode)
                .saleDate(customDate)
                .build();

        Method onCreateMethod = SaleEntity.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        onCreateMethod.invoke(entity);

        assertEquals(customCode, entity.getSaleCode());
        assertEquals(customDate, entity.getSaleDate());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("onUpdate pre-update callback should refresh updatedAt timestamp")
    void onUpdate_WhenTriggered_UpdatesUpdatedAtTimestamp() throws Exception {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(2);
        SaleEntity entity = SaleEntity.builder()
                .updatedAt(pastTime)
                .build();

        assertEquals(pastTime, entity.getUpdatedAt());

        Method onUpdateMethod = SaleEntity.class.getDeclaredMethod("onUpdate");
        onUpdateMethod.setAccessible(true);
        onUpdateMethod.invoke(entity);

        assertNotNull(entity.getUpdatedAt());
        assertTrue(entity.getUpdatedAt().isAfter(pastTime));
    }

    @Test
    @DisplayName("SaleEntity and SaleItemEntity getters, setters and builders should function correctly")
    void entityGettersSettersAndBuilders_WorkAsExpected() {
        SaleEntity saleEntity = new SaleEntity();
        UUID code = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        saleEntity.setId(10L);
        saleEntity.setSaleCode(code);
        saleEntity.setTotalAmount(new BigDecimal("500.00"));
        saleEntity.setAmountReceived(new BigDecimal("500.00"));
        saleEntity.setChangeGiven(BigDecimal.ZERO);
        saleEntity.setPaymentMethodId(1L);
        saleEntity.setStatusId(2L);
        saleEntity.setClientId(3L);
        saleEntity.setOutletId(4L);
        saleEntity.setEmployeeId(5L);
        saleEntity.setSaleDate(now);
        saleEntity.setCreatedAt(now);
        saleEntity.setUpdatedAt(now);

        SaleItemEntity itemEntity = new SaleItemEntity();
        itemEntity.setId(100L);
        itemEntity.setProductId(50L);
        itemEntity.setQuantityInBaseUnits(2);
        itemEntity.setDisplayQuantity("2 UND");
        itemEntity.setUnitPrice(new BigDecimal("250.00"));
        itemEntity.setSubtotal(new BigDecimal("500.00"));
        itemEntity.setUnitMeasureId(1L);
        itemEntity.setSale(saleEntity);

        saleEntity.setItems(List.of(itemEntity));

        assertEquals(10L, saleEntity.getId());
        assertEquals(code, saleEntity.getSaleCode());
        assertEquals(new BigDecimal("500.00"), saleEntity.getTotalAmount());
        assertEquals(new BigDecimal("500.00"), saleEntity.getAmountReceived());
        assertEquals(BigDecimal.ZERO, saleEntity.getChangeGiven());
        assertEquals(1L, saleEntity.getPaymentMethodId());
        assertEquals(2L, saleEntity.getStatusId());
        assertEquals(3L, saleEntity.getClientId());
        assertEquals(4L, saleEntity.getOutletId());
        assertEquals(5L, saleEntity.getEmployeeId());
        assertEquals(now, saleEntity.getSaleDate());
        assertEquals(now, saleEntity.getCreatedAt());
        assertEquals(now, saleEntity.getUpdatedAt());

        assertEquals(1, saleEntity.getItems().size());
        SaleItemEntity retrievedItem = saleEntity.getItems().get(0);
        assertEquals(100L, retrievedItem.getId());
        assertEquals(50L, retrievedItem.getProductId());
        assertEquals(2, retrievedItem.getQuantityInBaseUnits());
        assertEquals("2 UND", retrievedItem.getDisplayQuantity());
        assertEquals(new BigDecimal("250.00"), retrievedItem.getUnitPrice());
        assertEquals(new BigDecimal("500.00"), retrievedItem.getSubtotal());
        assertEquals(1L, retrievedItem.getUnitMeasureId());
        assertEquals(saleEntity, retrievedItem.getSale());
    }
}
