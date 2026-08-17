package org.frias.avalon.domain.order.infrastructure.persistence.mapper;

import org.frias.avalon.domain.order.application.dto.OrderItemResponse;
import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderItemDomain;
import org.frias.avalon.domain.order.domain.OrderStatusHistoryDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderEntity;
import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderItemEntity;
import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderStatusHistoryEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component("omnichannelOrderMapper")
public class OrderMapper {

    public OrderDomain toDomain(OrderEntity entity, List<OrderItemEntity> itemEntities) {
        if (entity == null) return null;
        List<OrderItemDomain> items = itemEntities != null
                ? itemEntities.stream().map(this::toItemDomain).collect(Collectors.toList())
                : List.of();

        return OrderDomain.builder()
                .id(entity.getId())
                .orderCode(entity.getOrderCode())
                .customerId(entity.getCustomerId())
                .outletId(entity.getOutletId())
                .orderStatusId(entity.getOrderStatusId())
                .paymentStatusId(entity.getPaymentStatusId())
                .paymentMethodId(entity.getPaymentMethodId())
                .subtotal(entity.getSubtotal())
                .tax(entity.getTax())
                .total(entity.getTotal())
                .claimedByUserId(entity.getClaimedByUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .items(items)
                .build();
    }

    public OrderEntity toEntity(OrderDomain domain) {
        if (domain == null) return null;
        return OrderEntity.builder()
                .id(domain.getId())
                .orderCode(domain.getOrderCode())
                .customerId(domain.getCustomerId())
                .outletId(domain.getOutletId())
                .orderStatusId(domain.getOrderStatusId())
                .paymentStatusId(domain.getPaymentStatusId())
                .paymentMethodId(domain.getPaymentMethodId())
                .subtotal(domain.getSubtotal())
                .tax(domain.getTax())
                .total(domain.getTotal())
                .claimedByUserId(domain.getClaimedByUserId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public OrderItemDomain toItemDomain(OrderItemEntity entity) {
        if (entity == null) return null;
        return OrderItemDomain.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .productOutletId(entity.getProductOutletId())
                .productName(entity.getProductName())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .subtotal(entity.getSubtotal())
                .dispatchStatusId(entity.getDispatchStatusId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public OrderItemEntity toItemEntity(OrderItemDomain domain) {
        if (domain == null) return null;
        return OrderItemEntity.builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .productOutletId(domain.getProductOutletId())
                .productName(domain.getProductName())
                .quantity(domain.getQuantity())
                .unitPrice(domain.getUnitPrice())
                .subtotal(domain.getSubtotal())
                .dispatchStatusId(domain.getDispatchStatusId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public OrderStatusHistoryEntity toStatusHistoryEntity(OrderStatusHistoryDomain domain) {
        if (domain == null) return null;
        return OrderStatusHistoryEntity.builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .previousStatusId(domain.getPreviousStatusId())
                .newStatusId(domain.getNewStatusId())
                .changedByUserId(domain.getChangedByUserId())
                .notes(domain.getNotes())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public OrderItemResponse toItemResponse(OrderItemDomain domain) {
        if (domain == null) return null;
        return OrderItemResponse.builder()
                .id(domain.getId())
                .productOutletId(domain.getProductOutletId())
                .productName(domain.getProductName())
                .quantity(domain.getQuantity())
                .unitPrice(domain.getUnitPrice())
                .subtotal(domain.getSubtotal())
                .dispatchStatusId(domain.getDispatchStatusId())
                .build();
    }

    public OrderResponse toResponse(OrderDomain domain) {
        if (domain == null) return null;
        List<OrderItemResponse> itemResponses = domain.getItems() != null
                ? domain.getItems().stream().map(this::toItemResponse).collect(Collectors.toList())
                : List.of();

        return OrderResponse.builder()
                .id(domain.getId())
                .orderCode(domain.getOrderCode())
                .customerId(domain.getCustomerId())
                .outletId(domain.getOutletId())
                .orderStatusId(domain.getOrderStatusId())
                .paymentStatusId(domain.getPaymentStatusId())
                .paymentMethodId(domain.getPaymentMethodId())
                .subtotal(domain.getSubtotal())
                .tax(domain.getTax())
                .total(domain.getTotal())
                .claimedByUserId(domain.getClaimedByUserId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .items(itemResponses)
                .build();
    }
}
