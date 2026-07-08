package org.frias.avalon.domain.sale.infrastructure.mapper;

import org.frias.avalon.domain.sale.domain.OrderDomain;
import org.frias.avalon.domain.sale.domain.OrderItemDomain;
import org.frias.avalon.domain.sale.infrastructure.entity.OrderEntity;
import org.frias.avalon.domain.sale.infrastructure.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderEntity toEntity(OrderDomain domain) {
        if (domain == null) return null;

        OrderEntity entity = OrderEntity.builder()
                .id(domain.getId())
                .orderCode(domain.getOrderCode())
                .totalAmount(domain.getTotalAmount())
                .paymentMethodId(domain.getPaymentMethodId())
                .statusId(domain.getStatusId())
                .outletId(domain.getOutletId())
                .orderDate(domain.getOrderDate())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();

        if (domain.getItems() != null) {
            List<OrderItemEntity> itemEntities = domain.getItems().stream()
                    .map(item -> toItemEntity(item, entity))
                    .collect(Collectors.toList());
            entity.setItems(itemEntities);
        }

        return entity;
    }

    public OrderItemEntity toItemEntity(OrderItemDomain domain, OrderEntity orderEntity) {
        if (domain == null) return null;

        return OrderItemEntity.builder()
                .id(domain.getId())
                .productId(domain.getProductId())
                .quantityInBaseUnits(domain.getQuantityInBaseUnits())
                .displayQuantity(domain.getDisplayQuantity())
                .unitPrice(domain.getUnitPrice())
                .subtotal(domain.getSubtotal())
                .unitMeasureId(domain.getUnitMeasureId())
                .order(orderEntity)
                .build();
    }

    public OrderDomain toDomain(OrderEntity entity) {
        if (entity == null) return null;

        List<OrderItemDomain> items = entity.getItems() != null
                ? entity.getItems().stream().map(this::toItemDomain).collect(Collectors.toList())
                : List.of();

        return OrderDomain.fromPersistence(
                entity.getId(),
                entity.getOrderCode(),
                entity.getTotalAmount(),
                entity.getPaymentMethodId(),
                entity.getStatusId(),
                entity.getOutletId(),
                entity.getOrderDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                items
        );
    }

    public OrderItemDomain toItemDomain(OrderItemEntity entity) {
        if (entity == null) return null;

        return new OrderItemDomain(
                entity.getId(),
                entity.getProductId(),
                entity.getQuantityInBaseUnits(),
                entity.getDisplayQuantity(),
                entity.getUnitPrice(),
                entity.getSubtotal(),
                entity.getUnitMeasureId()
        );
    }
}
