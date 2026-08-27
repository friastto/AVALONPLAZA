package org.frias.avalon.domain.sale.infrastructure.adapter;

import org.frias.avalon.domain.sale.domain.OrderDomain;
import org.frias.avalon.domain.sale.domain.OrderItemDomain;
import org.frias.avalon.domain.sale.infrastructure.entity.OrderEntity;
import org.frias.avalon.domain.sale.infrastructure.entity.OrderItemEntity;
import org.frias.avalon.domain.sale.infrastructure.mapper.OrderMapper;
import org.frias.avalon.domain.sale.infrastructure.repository.JpaOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para OrderRepositoryAdapter.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for OrderRepositoryAdapter")
class OrderRepositoryAdapterTest {

    @Mock
    private JpaOrderRepository jpaOrderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderRepositoryAdapter orderRepositoryAdapter;

    private OrderEntity sampleEntity;
    private OrderDomain sampleDomain;
    private UUID sampleUuid;
    private LocalDateTime sampleTime;

    @BeforeEach
    void setUp() {
        sampleUuid = UUID.randomUUID();
        sampleTime = LocalDateTime.now();

        OrderItemDomain itemDomain = new OrderItemDomain(
                1L, 100L, 2, "2 UND",
                new BigDecimal("50.00"), new BigDecimal("100.00"), 5L
        );

        sampleDomain = OrderDomain.fromPersistence(
                10L, sampleUuid, new BigDecimal("100.00"),
                1L, 2L, 3L,
                sampleTime, sampleTime, sampleTime, List.of(itemDomain)
        );

        OrderItemEntity itemEntity = OrderItemEntity.builder()
                .id(1L)
                .productId(100L)
                .quantityInBaseUnits(2)
                .displayQuantity("2 UND")
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("100.00"))
                .unitMeasureId(5L)
                .build();

        sampleEntity = OrderEntity.builder()
                .id(10L)
                .orderCode(sampleUuid)
                .totalAmount(new BigDecimal("100.00"))
                .paymentMethodId(1L)
                .statusId(2L)
                .outletId(3L)
                .orderDate(sampleTime)
                .createdAt(sampleTime)
                .updatedAt(sampleTime)
                .items(List.of(itemEntity))
                .build();
    }

    @Test
    @DisplayName("save should map to entity, invoke repository save, and map back to domain")
    void save_WithValidDomain_MapsSavesAndReturnsDomain() {
        OrderEntity mappedEntity = OrderEntity.builder().orderCode(sampleUuid).build();
        OrderEntity savedEntity = sampleEntity;

        when(orderMapper.toEntity(sampleDomain)).thenReturn(mappedEntity);
        when(jpaOrderRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(orderMapper.toDomain(savedEntity)).thenReturn(sampleDomain);

        OrderDomain result = orderRepositoryAdapter.save(sampleDomain);

        assertNotNull(result);
        assertEquals(sampleDomain.getId(), result.getId());
        assertEquals(sampleDomain.getOrderCode(), result.getOrderCode());

        verify(orderMapper, times(1)).toEntity(sampleDomain);
        verify(jpaOrderRepository, times(1)).save(mappedEntity);
        verify(orderMapper, times(1)).toDomain(savedEntity);
    }

    @Test
    @DisplayName("findByCode should return mapped domain when order is found")
    void findByCode_WhenExists_ReturnsMappedDomain() {
        when(jpaOrderRepository.findByOrderCode(sampleUuid)).thenReturn(Optional.of(sampleEntity));
        when(orderMapper.toDomain(sampleEntity)).thenReturn(sampleDomain);

        Optional<OrderDomain> result = orderRepositoryAdapter.findByCode(sampleUuid);

        assertTrue(result.isPresent());
        assertEquals(sampleDomain.getId(), result.get().getId());
        assertEquals(sampleUuid, result.get().getOrderCode());

        verify(jpaOrderRepository, times(1)).findByOrderCode(sampleUuid);
        verify(orderMapper, times(1)).toDomain(sampleEntity);
    }

    @Test
    @DisplayName("findByCode should return empty optional when order code is not found")
    void findByCode_WhenNotFound_ReturnsEmptyOptional() {
        UUID nonExistentCode = UUID.randomUUID();
        when(jpaOrderRepository.findByOrderCode(nonExistentCode)).thenReturn(Optional.empty());

        Optional<OrderDomain> result = orderRepositoryAdapter.findByCode(nonExistentCode);

        assertTrue(result.isEmpty());

        verify(jpaOrderRepository, times(1)).findByOrderCode(nonExistentCode);
        verify(orderMapper, never()).toDomain(any());
    }
}
