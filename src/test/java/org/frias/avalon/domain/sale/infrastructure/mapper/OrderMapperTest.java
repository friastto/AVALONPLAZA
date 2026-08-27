package org.frias.avalon.domain.sale.infrastructure.mapper;

import org.frias.avalon.domain.sale.domain.OrderDomain;
import org.frias.avalon.domain.sale.domain.OrderItemDomain;
import org.frias.avalon.domain.sale.infrastructure.entity.OrderEntity;
import org.frias.avalon.domain.sale.infrastructure.entity.OrderItemEntity;
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
 * Pruebas unitarias para OrderMapper.
 */
@DisplayName("Unit Tests for OrderMapper")
class OrderMapperTest {

    private OrderMapper orderMapper;
    private UUID sampleUuid;
    private LocalDateTime sampleTime;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapper();
        sampleUuid = UUID.randomUUID();
        sampleTime = LocalDateTime.now();
    }

    @Test
    @DisplayName("toEntity should return null when OrderDomain is null")
    void toEntity_WhenDomainIsNull_ReturnsNull() {
        assertNull(orderMapper.toEntity(null));
    }

    @Test
    @DisplayName("toEntity should map all OrderDomain properties and set OrderEntity reference on items")
    void toEntity_WhenDomainIsValid_MapsAllPropertiesAndItems() {
        OrderItemDomain itemDomain = new OrderItemDomain(
                101L, 50L, 3, "3 UND",
                new BigDecimal("25.00"), new BigDecimal("75.00"), 2L
        );

        OrderDomain domain = OrderDomain.fromPersistence(
                10L, sampleUuid, new BigDecimal("75.00"),
                1L, 2L, 3L,
                sampleTime, sampleTime, sampleTime, List.of(itemDomain)
        );

        OrderEntity entity = orderMapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(10L, entity.getId());
        assertEquals(sampleUuid, entity.getOrderCode());
        assertEquals(new BigDecimal("75.00"), entity.getTotalAmount());
        assertEquals(1L, entity.getPaymentMethodId());
        assertEquals(2L, entity.getStatusId());
        assertEquals(3L, entity.getOutletId());
        assertEquals(sampleTime, entity.getOrderDate());
        assertEquals(sampleTime, entity.getCreatedAt());
        assertEquals(sampleTime, entity.getUpdatedAt());

        assertNotNull(entity.getItems());
        assertEquals(1, entity.getItems().size());

        OrderItemEntity itemEntity = entity.getItems().get(0);
        assertEquals(101L, itemEntity.getId());
        assertEquals(50L, itemEntity.getProductId());
        assertEquals(3, itemEntity.getQuantityInBaseUnits());
        assertEquals("3 UND", itemEntity.getDisplayQuantity());
        assertEquals(new BigDecimal("25.00"), itemEntity.getUnitPrice());
        assertEquals(new BigDecimal("75.00"), itemEntity.getSubtotal());
        assertEquals(2L, itemEntity.getUnitMeasureId());
        assertEquals(entity, itemEntity.getOrder());
    }

    @Test
    @DisplayName("toEntity should return OrderEntity without items when domain.getItems() is null")
    void toEntity_WhenDomainItemsIsNull_ReturnsEntityWithNullItems() {
        OrderDomain mockDomain = mock(OrderDomain.class);
        when(mockDomain.getId()).thenReturn(15L);
        when(mockDomain.getOrderCode()).thenReturn(sampleUuid);
        when(mockDomain.getItems()).thenReturn(null);

        OrderEntity entity = orderMapper.toEntity(mockDomain);

        assertNotNull(entity);
        assertEquals(15L, entity.getId());
        assertEquals(sampleUuid, entity.getOrderCode());
        assertTrue(entity.getItems() == null || entity.getItems().isEmpty());
    }

    @Test
    @DisplayName("toItemEntity should return null when OrderItemDomain is null")
    void toItemEntity_WhenDomainIsNull_ReturnsNull() {
        OrderEntity orderEntity = OrderEntity.builder().id(10L).build();
        assertNull(orderMapper.toItemEntity(null, orderEntity));
    }

    @Test
    @DisplayName("toItemEntity should map OrderItemDomain to OrderItemEntity correctly")
    void toItemEntity_WhenDomainIsValid_MapsAllProperties() {
        OrderEntity orderEntity = OrderEntity.builder().id(10L).build();
        OrderItemDomain itemDomain = new OrderItemDomain(
                201L, 60L, 5, "5 KG",
                new BigDecimal("12.50"), new BigDecimal("62.50"), 4L
        );

        OrderItemEntity itemEntity = orderMapper.toItemEntity(itemDomain, orderEntity);

        assertNotNull(itemEntity);
        assertEquals(201L, itemEntity.getId());
        assertEquals(60L, itemEntity.getProductId());
        assertEquals(5, itemEntity.getQuantityInBaseUnits());
        assertEquals("5 KG", itemEntity.getDisplayQuantity());
        assertEquals(new BigDecimal("12.50"), itemEntity.getUnitPrice());
        assertEquals(new BigDecimal("62.50"), itemEntity.getSubtotal());
        assertEquals(4L, itemEntity.getUnitMeasureId());
        assertEquals(orderEntity, itemEntity.getOrder());
    }

    @Test
    @DisplayName("toDomain should return null when OrderEntity is null")
    void toDomain_WhenEntityIsNull_ReturnsNull() {
        assertNull(orderMapper.toDomain((OrderEntity) null));
    }

    @Test
    @DisplayName("toDomain should map OrderEntity to OrderDomain including items")
    void toDomain_WhenEntityIsValid_MapsAllPropertiesAndItems() {
        OrderItemEntity itemEntity = OrderItemEntity.builder()
                .id(301L)
                .productId(70L)
                .quantityInBaseUnits(1)
                .displayQuantity("1 CAJA")
                .unitPrice(new BigDecimal("200.00"))
                .subtotal(new BigDecimal("200.00"))
                .unitMeasureId(10L)
                .build();

        OrderEntity entity = OrderEntity.builder()
                .id(20L)
                .orderCode(sampleUuid)
                .totalAmount(new BigDecimal("200.00"))
                .paymentMethodId(1L)
                .statusId(2L)
                .outletId(3L)
                .orderDate(sampleTime)
                .createdAt(sampleTime)
                .updatedAt(sampleTime)
                .items(List.of(itemEntity))
                .build();

        OrderDomain domain = orderMapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(20L, domain.getId());
        assertEquals(sampleUuid, domain.getOrderCode());
        assertEquals(new BigDecimal("200.00"), domain.getTotalAmount());
        assertEquals(1L, domain.getPaymentMethodId());
        assertEquals(2L, domain.getStatusId());
        assertEquals(3L, domain.getOutletId());
        assertEquals(sampleTime, domain.getOrderDate());
        assertEquals(sampleTime, domain.getCreatedAt());
        assertEquals(sampleTime, domain.getUpdatedAt());

        assertNotNull(domain.getItems());
        assertEquals(1, domain.getItems().size());

        OrderItemDomain itemDomain = domain.getItems().get(0);
        assertEquals(301L, itemDomain.getId());
        assertEquals(70L, itemDomain.getProductId());
        assertEquals(1, itemDomain.getQuantityInBaseUnits());
        assertEquals("1 CAJA", itemDomain.getDisplayQuantity());
        assertEquals(new BigDecimal("200.00"), itemDomain.getUnitPrice());
        assertEquals(new BigDecimal("200.00"), itemDomain.getSubtotal());
        assertEquals(10L, itemDomain.getUnitMeasureId());
    }

    @Test
    @DisplayName("toDomain should return OrderDomain with empty items list when OrderEntity items is null")
    void toDomain_WhenEntityItemsIsNull_ReturnsEmptyItemList() {
        OrderEntity entity = OrderEntity.builder()
                .id(30L)
                .orderCode(sampleUuid)
                .totalAmount(new BigDecimal("150.00"))
                .paymentMethodId(1L)
                .statusId(2L)
                .outletId(3L)
                .orderDate(sampleTime)
                .createdAt(sampleTime)
                .updatedAt(sampleTime)
                .items(null)
                .build();

        OrderDomain domain = orderMapper.toDomain(entity);

        assertNotNull(domain);
        assertNotNull(domain.getItems());
        assertTrue(domain.getItems().isEmpty());
    }

    @Test
    @DisplayName("toItemDomain should return null when OrderItemEntity is null")
    void toItemDomain_WhenEntityIsNull_ReturnsNull() {
        assertNull(orderMapper.toItemDomain(null));
    }

    @Test
    @DisplayName("toItemDomain should map OrderItemEntity to OrderItemDomain correctly")
    void toItemDomain_WhenEntityIsValid_MapsAllProperties() {
        OrderItemEntity itemEntity = OrderItemEntity.builder()
                .id(401L)
                .productId(80L)
                .quantityInBaseUnits(10)
                .displayQuantity("10 LTRS")
                .unitPrice(new BigDecimal("15.00"))
                .subtotal(new BigDecimal("150.00"))
                .unitMeasureId(7L)
                .build();

        OrderItemDomain itemDomain = orderMapper.toItemDomain(itemEntity);

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
