package org.frias.avalon.domain.sale.infrastructure.mapper;

import org.frias.avalon.domain.sale.domain.ReturnDomain;
import org.frias.avalon.domain.sale.domain.ReturnItemDomain;
import org.frias.avalon.domain.sale.infrastructure.entity.ReturnEntity;
import org.frias.avalon.domain.sale.infrastructure.entity.ReturnItemEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para ReturnMapper.
 */
@DisplayName("Unit Tests for ReturnMapper")
class ReturnMapperTest {

    private ReturnMapper returnMapper;
    private UUID sampleUuid;
    private LocalDateTime sampleTime;

    @BeforeEach
    void setUp() {
        returnMapper = new ReturnMapper();
        sampleUuid = UUID.randomUUID();
        sampleTime = LocalDateTime.now();
    }

    @Test
    @DisplayName("toEntity should return null when ReturnDomain is null")
    void toEntity_WhenDomainIsNull_ReturnsNull() {
        assertNull(returnMapper.toEntity(null));
    }

    @Test
    @DisplayName("toEntity should map all ReturnDomain properties and set ReturnEntity reference on items")
    void toEntity_WhenDomainIsValid_MapsAllPropertiesAndItems() {
        ReturnItemDomain itemDomain = new ReturnItemDomain(
                101L, 50L, 3, "3 UND",
                new BigDecimal("25.00"), new BigDecimal("75.00"), 2L
        );

        ReturnDomain domain = ReturnDomain.fromPersistence(
                10L, sampleUuid, 1000L, new BigDecimal("75.00"),
                "DEFECTO", "Producto roto", "REEMBOLSO",
                1L, 2L, 3L, 4L,
                sampleTime, sampleTime, sampleTime, List.of(itemDomain)
        );

        ReturnEntity entity = returnMapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(10L, entity.getId());
        assertEquals(sampleUuid, entity.getReturnCode());
        assertEquals(1000L, entity.getOriginalSaleId());
        assertEquals(new BigDecimal("75.00"), entity.getTotalRefundAmount());
        assertEquals("DEFECTO", entity.getReason());
        assertEquals("Producto roto", entity.getNotes());
        assertEquals("REEMBOLSO", entity.getResolutionType());
        assertEquals(1L, entity.getStatusId());
        assertEquals(2L, entity.getEmployeeId());
        assertEquals(3L, entity.getOutletId());
        assertEquals(4L, entity.getClientId());
        assertEquals(sampleTime, entity.getReturnDate());
        assertEquals(sampleTime, entity.getCreatedAt());
        assertEquals(sampleTime, entity.getUpdatedAt());

        assertNotNull(entity.getItems());
        assertEquals(1, entity.getItems().size());

        ReturnItemEntity itemEntity = entity.getItems().get(0);
        assertEquals(101L, itemEntity.getId());
        assertEquals(50L, itemEntity.getProductId());
        assertEquals(3, itemEntity.getQuantityInBaseUnits());
        assertEquals("3 UND", itemEntity.getDisplayQuantity());
        assertEquals(new BigDecimal("25.00"), itemEntity.getUnitPrice());
        assertEquals(new BigDecimal("75.00"), itemEntity.getSubtotal());
        assertEquals(2L, itemEntity.getUnitMeasureId());
        assertEquals(entity, itemEntity.getReturnEntity());
    }

    @Test
    @DisplayName("toEntity should return ReturnEntity without items when domain.getItems() is null")
    void toEntity_WhenDomainItemsIsNull_ReturnsEntityWithNullItems() {
        ReturnDomain mockDomain = mock(ReturnDomain.class);
        when(mockDomain.getId()).thenReturn(15L);
        when(mockDomain.getReturnCode()).thenReturn(sampleUuid);
        when(mockDomain.getItems()).thenReturn(null);

        ReturnEntity entity = returnMapper.toEntity(mockDomain);

        assertNotNull(entity);
        assertEquals(15L, entity.getId());
        assertEquals(sampleUuid, entity.getReturnCode());
        assertTrue(entity.getItems() == null || entity.getItems().isEmpty());
    }

    @Test
    @DisplayName("toItemEntity should return null when ReturnItemDomain is null")
    void toItemEntity_WhenDomainIsNull_ReturnsNull() {
        ReturnEntity returnEntity = ReturnEntity.builder().id(10L).build();
        assertNull(returnMapper.toItemEntity(null, returnEntity));
    }

    @Test
    @DisplayName("toItemEntity should map ReturnItemDomain to ReturnItemEntity correctly")
    void toItemEntity_WhenDomainIsValid_MapsAllProperties() {
        ReturnEntity returnEntity = ReturnEntity.builder().id(10L).build();
        ReturnItemDomain itemDomain = new ReturnItemDomain(
                201L, 60L, 5, "5 KG",
                new BigDecimal("12.50"), new BigDecimal("62.50"), 4L
        );

        ReturnItemEntity itemEntity = returnMapper.toItemEntity(itemDomain, returnEntity);

        assertNotNull(itemEntity);
        assertEquals(201L, itemEntity.getId());
        assertEquals(60L, itemEntity.getProductId());
        assertEquals(5, itemEntity.getQuantityInBaseUnits());
        assertEquals("5 KG", itemEntity.getDisplayQuantity());
        assertEquals(new BigDecimal("12.50"), itemEntity.getUnitPrice());
        assertEquals(new BigDecimal("62.50"), itemEntity.getSubtotal());
        assertEquals(4L, itemEntity.getUnitMeasureId());
        assertEquals(returnEntity, itemEntity.getReturnEntity());
    }

    @Test
    @DisplayName("toDomain should return null when ReturnEntity is null")
    void toDomain_WhenEntityIsNull_ReturnsNull() {
        assertNull(returnMapper.toDomain((ReturnEntity) null));
    }

    @Test
    @DisplayName("toDomain should map ReturnEntity to ReturnDomain including items")
    void toDomain_WhenEntityIsValid_MapsAllPropertiesAndItems() {
        ReturnItemEntity itemEntity = ReturnItemEntity.builder()
                .id(301L)
                .productId(70L)
                .quantityInBaseUnits(1)
                .displayQuantity("1 CAJA")
                .unitPrice(new BigDecimal("200.00"))
                .subtotal(new BigDecimal("200.00"))
                .unitMeasureId(10L)
                .build();

        ReturnEntity entity = ReturnEntity.builder()
                .id(20L)
                .returnCode(sampleUuid)
                .originalSaleId(2000L)
                .totalRefundAmount(new BigDecimal("200.00"))
                .reason("INCORRECTO")
                .notes("Item equivocado")
                .resolutionType("CAMBIO")
                .statusId(1L)
                .employeeId(2L)
                .outletId(3L)
                .clientId(4L)
                .returnDate(sampleTime)
                .createdAt(sampleTime)
                .updatedAt(sampleTime)
                .items(List.of(itemEntity))
                .build();

        ReturnDomain domain = returnMapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(20L, domain.getId());
        assertEquals(sampleUuid, domain.getReturnCode());
        assertEquals(2000L, domain.getOriginalSaleId());
        assertEquals(new BigDecimal("200.00"), domain.getTotalRefundAmount());
        assertEquals("INCORRECTO", domain.getReason());
        assertEquals("Item equivocado", domain.getNotes());
        assertEquals("CAMBIO", domain.getResolutionType());
        assertEquals(1L, domain.getStatusId());
        assertEquals(2L, domain.getEmployeeId());
        assertEquals(3L, domain.getOutletId());
        assertEquals(4L, domain.getClientId());
        assertEquals(sampleTime, domain.getReturnDate());
        assertEquals(sampleTime, domain.getCreatedAt());
        assertEquals(sampleTime, domain.getUpdatedAt());

        assertNotNull(domain.getItems());
        assertEquals(1, domain.getItems().size());

        ReturnItemDomain itemDomain = domain.getItems().get(0);
        assertEquals(301L, itemDomain.getId());
        assertEquals(70L, itemDomain.getProductId());
        assertEquals(1, itemDomain.getQuantityInBaseUnits());
        assertEquals("1 CAJA", itemDomain.getDisplayQuantity());
        assertEquals(new BigDecimal("200.00"), itemDomain.getUnitPrice());
        assertEquals(new BigDecimal("200.00"), itemDomain.getSubtotal());
        assertEquals(10L, itemDomain.getUnitMeasureId());
    }

    @Test
    @DisplayName("toDomain should return ReturnDomain with empty items list when ReturnEntity items is null")
    void toDomain_WhenEntityItemsIsNull_ReturnsEmptyItemList() {
        ReturnEntity entity = ReturnEntity.builder()
                .id(30L)
                .returnCode(sampleUuid)
                .originalSaleId(3000L)
                .totalRefundAmount(new BigDecimal("150.00"))
                .reason("OTRO")
                .notes("Obs")
                .resolutionType("NOTA_CREDITO")
                .statusId(1L)
                .employeeId(2L)
                .outletId(3L)
                .clientId(4L)
                .returnDate(sampleTime)
                .createdAt(sampleTime)
                .updatedAt(sampleTime)
                .items(null)
                .build();

        ReturnDomain domain = returnMapper.toDomain(entity);

        assertNotNull(domain);
        assertNotNull(domain.getItems());
        assertTrue(domain.getItems().isEmpty());
    }

    @Test
    @DisplayName("toItemDomain should return null when ReturnItemEntity is null")
    void toItemDomain_WhenEntityIsNull_ReturnsNull() {
        assertNull(returnMapper.toItemDomain(null));
    }

    @Test
    @DisplayName("toItemDomain should map ReturnItemEntity to ReturnItemDomain correctly")
    void toItemDomain_WhenEntityIsValid_MapsAllProperties() {
        ReturnItemEntity itemEntity = ReturnItemEntity.builder()
                .id(401L)
                .productId(80L)
                .quantityInBaseUnits(10)
                .displayQuantity("10 LTRS")
                .unitPrice(new BigDecimal("15.00"))
                .subtotal(new BigDecimal("150.00"))
                .unitMeasureId(7L)
                .build();

        ReturnItemDomain itemDomain = returnMapper.toItemDomain(itemEntity);

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
