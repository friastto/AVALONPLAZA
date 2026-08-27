package org.frias.avalon.domain.sale.infrastructure.mapper;

import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.sale.domain.SaleItemDomain;
import org.frias.avalon.domain.sale.infrastructure.entity.SaleEntity;
import org.frias.avalon.domain.sale.infrastructure.entity.SaleItemEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for SaleMapper")
class SaleMapperTest {

    private SaleMapper saleMapper;
    private UUID sampleUuid;
    private LocalDateTime sampleTime;

    @BeforeEach
    void setUp() {
        saleMapper = new SaleMapper();
        sampleUuid = UUID.randomUUID();
        sampleTime = LocalDateTime.now();
    }

    @Test
    @DisplayName("toEntity should return null when SaleDomain is null")
    void toEntity_WhenDomainIsNull_ReturnsNull() {
        assertNull(saleMapper.toEntity(null));
    }

    @Test
    @DisplayName("toEntity should map all SaleDomain properties and set bidirectional SaleEntity reference on items")
    void toEntity_WhenDomainIsValid_MapsAllPropertiesAndItems() {
        SaleItemDomain itemDomain = new SaleItemDomain(
                101L, 50L, 3, "3 UND",
                new BigDecimal("25.00"), new BigDecimal("75.00"), 2L
        );

        SaleDomain domain = SaleDomain.fromPersistence(
                10L,
                sampleUuid,
                new BigDecimal("75.00"),
                new BigDecimal("100.00"),
                new BigDecimal("25.00"),
                1L,
                2L,
                3L,
                4L,
                5L,
                sampleTime,
                sampleTime,
                sampleTime,
                List.of(itemDomain)
        );

        SaleEntity entity = saleMapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(10L, entity.getId());
        assertEquals(sampleUuid, entity.getSaleCode());
        assertEquals(new BigDecimal("75.00"), entity.getTotalAmount());
        assertEquals(new BigDecimal("100.00"), entity.getAmountReceived());
        assertEquals(new BigDecimal("25.00"), entity.getChangeGiven());
        assertEquals(1L, entity.getPaymentMethodId());
        assertEquals(2L, entity.getStatusId());
        assertEquals(3L, entity.getClientId());
        assertEquals(4L, entity.getOutletId());
        assertEquals(5L, entity.getEmployeeId());
        assertEquals(sampleTime, entity.getSaleDate());
        assertEquals(sampleTime, entity.getCreatedAt());
        assertEquals(sampleTime, entity.getUpdatedAt());

        assertNotNull(entity.getItems());
        assertEquals(1, entity.getItems().size());

        SaleItemEntity itemEntity = entity.getItems().get(0);
        assertEquals(101L, itemEntity.getId());
        assertEquals(50L, itemEntity.getProductId());
        assertEquals(3, itemEntity.getQuantityInBaseUnits());
        assertEquals("3 UND", itemEntity.getDisplayQuantity());
        assertEquals(new BigDecimal("25.00"), itemEntity.getUnitPrice());
        assertEquals(new BigDecimal("75.00"), itemEntity.getSubtotal());
        assertEquals(2L, itemEntity.getUnitMeasureId());
        // Verify bidirectional relationship: SaleItemEntity.sale must point back to entity
        assertEquals(entity, itemEntity.getSale());
    }

    @Test
    @DisplayName("toItemEntity should return null when SaleItemDomain is null")
    void toItemEntity_WhenDomainIsNull_ReturnsNull() {
        SaleEntity saleEntity = SaleEntity.builder().id(10L).build();
        assertNull(saleMapper.toItemEntity(null, saleEntity));
    }

    @Test
    @DisplayName("toItemEntity should map SaleItemDomain to SaleItemEntity correctly")
    void toItemEntity_WhenDomainIsValid_MapsAllProperties() {
        SaleEntity saleEntity = SaleEntity.builder().id(10L).build();
        SaleItemDomain itemDomain = new SaleItemDomain(
                201L, 60L, 5, "5 KG",
                new BigDecimal("12.50"), new BigDecimal("62.50"), 4L
        );

        SaleItemEntity itemEntity = saleMapper.toItemEntity(itemDomain, saleEntity);

        assertNotNull(itemEntity);
        assertEquals(201L, itemEntity.getId());
        assertEquals(60L, itemEntity.getProductId());
        assertEquals(5, itemEntity.getQuantityInBaseUnits());
        assertEquals("5 KG", itemEntity.getDisplayQuantity());
        assertEquals(new BigDecimal("12.50"), itemEntity.getUnitPrice());
        assertEquals(new BigDecimal("62.50"), itemEntity.getSubtotal());
        assertEquals(4L, itemEntity.getUnitMeasureId());
        assertEquals(saleEntity, itemEntity.getSale());
    }

    @Test
    @DisplayName("toDomain should return null when SaleEntity is null")
    void toDomain_WhenEntityIsNull_ReturnsNull() {
        assertNull(saleMapper.toDomain((SaleEntity) null));
    }

    @Test
    @DisplayName("toDomain should map SaleEntity to SaleDomain including items")
    void toDomain_WhenEntityIsValid_MapsAllPropertiesAndItems() {
        SaleItemEntity itemEntity = SaleItemEntity.builder()
                .id(301L)
                .productId(70L)
                .quantityInBaseUnits(1)
                .displayQuantity("1 CAJA")
                .unitPrice(new BigDecimal("200.00"))
                .subtotal(new BigDecimal("200.00"))
                .unitMeasureId(10L)
                .build();

        SaleEntity entity = SaleEntity.builder()
                .id(20L)
                .saleCode(sampleUuid)
                .totalAmount(new BigDecimal("200.00"))
                .amountReceived(new BigDecimal("200.00"))
                .changeGiven(BigDecimal.ZERO)
                .paymentMethodId(2L)
                .statusId(1L)
                .clientId(8L)
                .outletId(3L)
                .employeeId(4L)
                .saleDate(sampleTime)
                .createdAt(sampleTime)
                .updatedAt(sampleTime)
                .items(List.of(itemEntity))
                .build();

        SaleDomain domain = saleMapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(20L, domain.getId());
        assertEquals(sampleUuid, domain.getSaleCode());
        assertEquals(new BigDecimal("200.00"), domain.getTotalAmount());
        assertEquals(new BigDecimal("200.00"), domain.getAmountReceived());
        assertEquals(BigDecimal.ZERO, domain.getChangeGiven());
        assertEquals(2L, domain.getPaymentMethodId());
        assertEquals(1L, domain.getStatusId());
        assertEquals(8L, domain.getClientId());
        assertEquals(3L, domain.getOutletId());
        assertEquals(4L, domain.getEmployeeId());
        assertEquals(sampleTime, domain.getSaleDate());
        assertEquals(sampleTime, domain.getCreatedAt());
        assertEquals(sampleTime, domain.getUpdatedAt());

        assertNotNull(domain.getItems());
        assertEquals(1, domain.getItems().size());

        SaleItemDomain itemDomain = domain.getItems().get(0);
        assertEquals(301L, itemDomain.getId());
        assertEquals(70L, itemDomain.getProductId());
        assertEquals(1, itemDomain.getQuantityInBaseUnits());
        assertEquals("1 CAJA", itemDomain.getDisplayQuantity());
        assertEquals(new BigDecimal("200.00"), itemDomain.getUnitPrice());
        assertEquals(new BigDecimal("200.00"), itemDomain.getSubtotal());
        assertEquals(10L, itemDomain.getUnitMeasureId());
    }

    @Test
    @DisplayName("toDomain should return SaleDomain with empty items list when SaleEntity items is null")
    void toDomain_WhenEntityItemsIsNull_ReturnsEmptyItemList() {
        SaleEntity entity = SaleEntity.builder()
                .id(30L)
                .saleCode(sampleUuid)
                .totalAmount(new BigDecimal("150.00"))
                .amountReceived(new BigDecimal("150.00"))
                .changeGiven(BigDecimal.ZERO)
                .paymentMethodId(1L)
                .statusId(1L)
                .clientId(2L)
                .outletId(3L)
                .employeeId(4L)
                .saleDate(sampleTime)
                .createdAt(sampleTime)
                .updatedAt(sampleTime)
                .items(null)
                .build();

        SaleDomain domain = saleMapper.toDomain(entity);

        assertNotNull(domain);
        assertNotNull(domain.getItems());
        assertTrue(domain.getItems().isEmpty());
    }

    @Test
    @DisplayName("toItemDomain should return null when SaleItemEntity is null")
    void toItemDomain_WhenEntityIsNull_ReturnsNull() {
        assertNull(saleMapper.toItemDomain(null));
    }

    @Test
    @DisplayName("toItemDomain should map SaleItemEntity to SaleItemDomain correctly")
    void toItemDomain_WhenEntityIsValid_MapsAllProperties() {
        SaleItemEntity itemEntity = SaleItemEntity.builder()
                .id(401L)
                .productId(80L)
                .quantityInBaseUnits(10)
                .displayQuantity("10 LTRS")
                .unitPrice(new BigDecimal("15.00"))
                .subtotal(new BigDecimal("150.00"))
                .unitMeasureId(7L)
                .build();

        SaleItemDomain itemDomain = saleMapper.toItemDomain(itemEntity);

        assertNotNull(itemDomain);
        assertEquals(401L, itemDomain.getId());
        assertEquals(80L, itemDomain.getProductId());
        assertEquals(10, itemDomain.getQuantityInBaseUnits());
        assertEquals("10 LTRS", itemDomain.getDisplayQuantity());
        assertEquals(new BigDecimal("15.00"), itemDomain.getUnitPrice());
        assertEquals(new BigDecimal("150.00"), itemDomain.getSubtotal());
        assertEquals(7L, itemDomain.getUnitMeasureId());
    }
}
