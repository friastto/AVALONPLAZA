package org.frias.avalon.domain.order.infrastructure.persistence.adapter;

import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderItemDomain;
import org.frias.avalon.domain.order.domain.OrderStatusHistoryDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderEntity;
import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderItemEntity;
import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderStatusHistoryEntity;
import org.frias.avalon.domain.order.infrastructure.persistence.mapper.OrderMapper;
import org.frias.avalon.domain.order.infrastructure.persistence.repository.JpaOrderItemRepository;
import org.frias.avalon.domain.order.infrastructure.persistence.repository.JpaOrderRepository;
import org.frias.avalon.domain.order.infrastructure.persistence.repository.JpaOrderStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for OrderPersistenceAdapter in Omnichannel Order Domain")
class OrderPersistenceAdapterTest {

    private JpaOrderRepository jpaOrderRepository;
    private JpaOrderItemRepository jpaOrderItemRepository;
    private JpaOrderStatusHistoryRepository jpaOrderStatusHistoryRepository;
    private OrderMapper orderMapper;

    private OrderPersistenceAdapter orderPersistenceAdapter;

    @BeforeEach
    void setUp() {
        jpaOrderRepository = mock(JpaOrderRepository.class);
        jpaOrderItemRepository = mock(JpaOrderItemRepository.class);
        jpaOrderStatusHistoryRepository = mock(JpaOrderStatusHistoryRepository.class);
        orderMapper = mock(OrderMapper.class);

        orderPersistenceAdapter = new OrderPersistenceAdapter(
                jpaOrderRepository,
                jpaOrderItemRepository,
                jpaOrderStatusHistoryRepository,
                orderMapper
        );
    }

    @Test
    @DisplayName("Should save order with items and map saved entity back to domain")
    void save_WithItems_SavesOrderAndItemsAndReturnsMappedDomain() {
        OrderItemDomain itemDomain1 = OrderItemDomain.builder()
                .productOutletId(10L)
                .productName("Producto A")
                .quantity(2)
                .unitPrice(new BigDecimal("15.00"))
                .subtotal(new BigDecimal("30.00"))
                .build();

        OrderDomain inputDomain = OrderDomain.builder()
                .orderCode("ORD-100")
                .customerId(5L)
                .outletId(1L)
                .items(List.of(itemDomain1))
                .build();

        OrderEntity mappedEntity = OrderEntity.builder()
                .orderCode("ORD-100")
                .customerId(5L)
                .outletId(1L)
                .build();

        OrderEntity savedEntity = OrderEntity.builder()
                .id(50L)
                .orderCode("ORD-100")
                .customerId(5L)
                .outletId(1L)
                .build();

        OrderItemEntity mappedItemEntity = OrderItemEntity.builder()
                .orderId(50L)
                .productOutletId(10L)
                .productName("Producto A")
                .quantity(2)
                .unitPrice(new BigDecimal("15.00"))
                .subtotal(new BigDecimal("30.00"))
                .build();

        OrderItemEntity savedItemEntity = OrderItemEntity.builder()
                .id(101L)
                .orderId(50L)
                .productOutletId(10L)
                .productName("Producto A")
                .quantity(2)
                .unitPrice(new BigDecimal("15.00"))
                .subtotal(new BigDecimal("30.00"))
                .build();

        OrderDomain expectedDomain = OrderDomain.builder()
                .id(50L)
                .orderCode("ORD-100")
                .customerId(5L)
                .outletId(1L)
                .items(List.of(itemDomain1))
                .build();

        when(orderMapper.toEntity(inputDomain)).thenReturn(mappedEntity);
        when(jpaOrderRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(orderMapper.toItemEntity(itemDomain1)).thenReturn(mappedItemEntity);
        when(jpaOrderItemRepository.save(mappedItemEntity)).thenReturn(savedItemEntity);
        when(jpaOrderItemRepository.findAllByOrderId(50L)).thenReturn(List.of(savedItemEntity));
        when(orderMapper.toDomain(savedEntity, List.of(savedItemEntity))).thenReturn(expectedDomain);

        OrderDomain result = orderPersistenceAdapter.save(inputDomain);

        assertNotNull(result);
        assertEquals(50L, result.getId());
        assertEquals("ORD-100", result.getOrderCode());
        assertEquals(50L, itemDomain1.getOrderId());
        verify(jpaOrderRepository, times(1)).save(mappedEntity);
        verify(jpaOrderItemRepository, times(1)).save(mappedItemEntity);
        verify(jpaOrderItemRepository, times(1)).findAllByOrderId(50L);
    }

    @Test
    @DisplayName("Should save order without items cleanly")
    void save_WithoutItems_SavesOrderAndReturnsMappedDomain() {
        OrderDomain inputDomain = OrderDomain.builder()
                .orderCode("ORD-200")
                .customerId(8L)
                .outletId(2L)
                .items(null)
                .build();

        OrderEntity mappedEntity = OrderEntity.builder().orderCode("ORD-200").build();
        OrderEntity savedEntity = OrderEntity.builder().id(60L).orderCode("ORD-200").build();
        OrderDomain expectedDomain = OrderDomain.builder().id(60L).orderCode("ORD-200").items(Collections.emptyList()).build();

        when(orderMapper.toEntity(inputDomain)).thenReturn(mappedEntity);
        when(jpaOrderRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(jpaOrderItemRepository.findAllByOrderId(60L)).thenReturn(Collections.emptyList());
        when(orderMapper.toDomain(savedEntity, Collections.emptyList())).thenReturn(expectedDomain);

        OrderDomain result = orderPersistenceAdapter.save(inputDomain);

        assertNotNull(result);
        assertEquals(60L, result.getId());
        verify(jpaOrderItemRepository, never()).save(any());
        verify(jpaOrderItemRepository, times(1)).findAllByOrderId(60L);
    }

    @Test
    @DisplayName("Should find order by ID when present")
    void findById_WhenExists_ReturnsMappedDomain() {
        Long orderId = 70L;
        OrderEntity entity = OrderEntity.builder().id(orderId).orderCode("ORD-70").build();
        OrderItemEntity itemEntity = OrderItemEntity.builder().id(100L).orderId(orderId).build();
        OrderDomain expectedDomain = OrderDomain.builder().id(orderId).orderCode("ORD-70").build();

        when(jpaOrderRepository.findById(orderId)).thenReturn(Optional.of(entity));
        when(jpaOrderItemRepository.findAllByOrderId(orderId)).thenReturn(List.of(itemEntity));
        when(orderMapper.toDomain(entity, List.of(itemEntity))).thenReturn(expectedDomain);

        Optional<OrderDomain> result = orderPersistenceAdapter.findById(orderId);

        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getId());
    }

    @Test
    @DisplayName("Should return empty optional when findById misses")
    void findById_WhenNotExists_ReturnsEmptyOptional() {
        Long orderId = 999L;
        when(jpaOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        Optional<OrderDomain> result = orderPersistenceAdapter.findById(orderId);

        assertTrue(result.isEmpty());
        verify(jpaOrderItemRepository, never()).findAllByOrderId(any());
    }

    @Test
    @DisplayName("Should find order by order code when present")
    void findByOrderCode_WhenExists_ReturnsMappedDomain() {
        String code = "ORD-ABC";
        OrderEntity entity = OrderEntity.builder().id(80L).orderCode(code).build();
        OrderDomain expectedDomain = OrderDomain.builder().id(80L).orderCode(code).build();

        when(jpaOrderRepository.findByOrderCode(code)).thenReturn(Optional.of(entity));
        when(jpaOrderItemRepository.findAllByOrderId(80L)).thenReturn(Collections.emptyList());
        when(orderMapper.toDomain(entity, Collections.emptyList())).thenReturn(expectedDomain);

        Optional<OrderDomain> result = orderPersistenceAdapter.findByOrderCode(code);

        assertTrue(result.isPresent());
        assertEquals(code, result.get().getOrderCode());
    }

    @Test
    @DisplayName("Should return empty optional when findByOrderCode misses")
    void findByOrderCode_WhenNotExists_ReturnsEmptyOptional() {
        String code = "NON_EXISTENT";
        when(jpaOrderRepository.findByOrderCode(code)).thenReturn(Optional.empty());

        Optional<OrderDomain> result = orderPersistenceAdapter.findByOrderCode(code);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find next pending order FIFO when present")
    void findNextPendingOrderFifo_WhenExists_ReturnsMappedDomain() {
        Long outletId = 1L;
        Long statusId = 101L;
        OrderEntity entity = OrderEntity.builder().id(90L).outletId(outletId).orderStatusId(statusId).build();
        OrderDomain expectedDomain = OrderDomain.builder().id(90L).outletId(outletId).orderStatusId(statusId).build();

        when(jpaOrderRepository.findNextPendingOrderFifo(outletId, statusId)).thenReturn(Optional.of(entity));
        when(jpaOrderItemRepository.findAllByOrderId(90L)).thenReturn(Collections.emptyList());
        when(orderMapper.toDomain(entity, Collections.emptyList())).thenReturn(expectedDomain);

        Optional<OrderDomain> result = orderPersistenceAdapter.findNextPendingOrderFifo(outletId, statusId);

        assertTrue(result.isPresent());
        assertEquals(90L, result.get().getId());
    }

    @Test
    @DisplayName("Should return empty optional when findNextPendingOrderFifo misses")
    void findNextPendingOrderFifo_WhenNotExists_ReturnsEmptyOptional() {
        Long outletId = 1L;
        Long statusId = 101L;
        when(jpaOrderRepository.findNextPendingOrderFifo(outletId, statusId)).thenReturn(Optional.empty());

        Optional<OrderDomain> result = orderPersistenceAdapter.findNextPendingOrderFifo(outletId, statusId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find all orders by outlet ID ordered by creation date descending")
    void findAllByOutletId_ReturnsMappedDomainList() {
        Long outletId = 2L;
        OrderEntity entity1 = OrderEntity.builder().id(10L).outletId(outletId).build();
        OrderEntity entity2 = OrderEntity.builder().id(11L).outletId(outletId).build();

        OrderDomain domain1 = OrderDomain.builder().id(10L).build();
        OrderDomain domain2 = OrderDomain.builder().id(11L).build();

        when(jpaOrderRepository.findAllByOutletIdOrderByCreatedAtDesc(outletId)).thenReturn(List.of(entity1, entity2));
        when(jpaOrderItemRepository.findAllByOrderId(10L)).thenReturn(Collections.emptyList());
        when(jpaOrderItemRepository.findAllByOrderId(11L)).thenReturn(Collections.emptyList());
        when(orderMapper.toDomain(entity1, Collections.emptyList())).thenReturn(domain1);
        when(orderMapper.toDomain(entity2, Collections.emptyList())).thenReturn(domain2);

        List<OrderDomain> results = orderPersistenceAdapter.findAllByOutletId(outletId);

        assertEquals(2, results.size());
        assertEquals(10L, results.get(0).getId());
        assertEquals(11L, results.get(1).getId());
    }

    @Test
    @DisplayName("Should find all orders by customer ID ordered by creation date descending")
    void findAllByCustomerId_ReturnsMappedDomainList() {
        Long customerId = 55L;
        OrderEntity entity = OrderEntity.builder().id(12L).customerId(customerId).build();
        OrderDomain domain = OrderDomain.builder().id(12L).customerId(customerId).build();

        when(jpaOrderRepository.findAllByCustomerIdOrderByCreatedAtDesc(customerId)).thenReturn(List.of(entity));
        when(jpaOrderItemRepository.findAllByOrderId(12L)).thenReturn(Collections.emptyList());
        when(orderMapper.toDomain(entity, Collections.emptyList())).thenReturn(domain);

        List<OrderDomain> results = orderPersistenceAdapter.findAllByCustomerId(customerId);

        assertEquals(1, results.size());
        assertEquals(12L, results.get(0).getId());
    }

    @Test
    @DisplayName("Should save order item and return mapped domain item")
    void saveItem_SavesAndReturnsMappedItemDomain() {
        OrderItemDomain inputItem = OrderItemDomain.builder()
                .orderId(50L)
                .productName("Galletas")
                .quantity(3)
                .build();

        OrderItemEntity entity = OrderItemEntity.builder()
                .orderId(50L)
                .productName("Galletas")
                .quantity(3)
                .build();

        OrderItemEntity savedEntity = OrderItemEntity.builder()
                .id(555L)
                .orderId(50L)
                .productName("Galletas")
                .quantity(3)
                .build();

        OrderItemDomain expectedItem = OrderItemDomain.builder()
                .id(555L)
                .orderId(50L)
                .productName("Galletas")
                .quantity(3)
                .build();

        when(orderMapper.toItemEntity(inputItem)).thenReturn(entity);
        when(jpaOrderItemRepository.save(entity)).thenReturn(savedEntity);
        when(orderMapper.toItemDomain(savedEntity)).thenReturn(expectedItem);

        OrderItemDomain result = orderPersistenceAdapter.saveItem(inputItem);

        assertNotNull(result);
        assertEquals(555L, result.getId());
        assertEquals(50L, result.getOrderId());
    }

    @Test
    @DisplayName("Should find item by ID when present")
    void findItemById_WhenExists_ReturnsMappedItemDomain() {
        Long itemId = 555L;
        OrderItemEntity entity = OrderItemEntity.builder().id(itemId).build();
        OrderItemDomain domain = OrderItemDomain.builder().id(itemId).build();

        when(jpaOrderItemRepository.findById(itemId)).thenReturn(Optional.of(entity));
        when(orderMapper.toItemDomain(entity)).thenReturn(domain);

        Optional<OrderItemDomain> result = orderPersistenceAdapter.findItemById(itemId);

        assertTrue(result.isPresent());
        assertEquals(itemId, result.get().getId());
    }

    @Test
    @DisplayName("Should return empty optional when findItemById misses")
    void findItemById_WhenNotExists_ReturnsEmptyOptional() {
        Long itemId = 999L;
        when(jpaOrderItemRepository.findById(itemId)).thenReturn(Optional.empty());

        Optional<OrderItemDomain> result = orderPersistenceAdapter.findItemById(itemId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should save order status history and return domain history")
    void saveStatusHistory_SavesAndReturnsMappedStatusHistoryDomain() {
        LocalDateTime now = LocalDateTime.now();
        OrderStatusHistoryDomain inputHistory = OrderStatusHistoryDomain.builder()
                .orderId(50L)
                .previousStatusId(1L)
                .newStatusId(2L)
                .changedByUserId(42L)
                .notes("Cambio de estado")
                .createdAt(now)
                .build();

        OrderStatusHistoryEntity mappedEntity = OrderStatusHistoryEntity.builder()
                .orderId(50L)
                .previousStatusId(1L)
                .newStatusId(2L)
                .changedByUserId(42L)
                .notes("Cambio de estado")
                .createdAt(now)
                .build();

        OrderStatusHistoryEntity savedEntity = OrderStatusHistoryEntity.builder()
                .id(777L)
                .orderId(50L)
                .previousStatusId(1L)
                .newStatusId(2L)
                .changedByUserId(42L)
                .notes("Cambio de estado")
                .createdAt(now)
                .build();

        when(orderMapper.toStatusHistoryEntity(inputHistory)).thenReturn(mappedEntity);
        when(jpaOrderStatusHistoryRepository.save(mappedEntity)).thenReturn(savedEntity);

        OrderStatusHistoryDomain result = orderPersistenceAdapter.saveStatusHistory(inputHistory);

        assertNotNull(result);
        assertEquals(777L, result.getId());
        assertEquals(50L, result.getOrderId());
        assertEquals(1L, result.getPreviousStatusId());
        assertEquals(2L, result.getNewStatusId());
        assertEquals(42L, result.getChangedByUserId());
        assertEquals("Cambio de estado", result.getNotes());
        assertEquals(now, result.getCreatedAt());
    }
}
