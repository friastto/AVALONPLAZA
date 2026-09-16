package org.frias.avalon.domain.order.infrastructure.persistence.mapper;

import org.frias.avalon.domain.order.application.dto.OrderItemResponse;
import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderItemDomain;
import org.frias.avalon.domain.order.domain.OrderStatusHistoryDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderEntity;
import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderItemEntity;
import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderStatusHistoryEntity;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component("omnichannelOrderMapper")
public class OrderMapper {

    private final UnitConversionService unitConversionService;
    private final JpaProductOutletRepository jpaProductOutletRepository;
    private final MasterTreeProvider masterTreeProvider;

    public OrderMapper() {
        this.unitConversionService = null;
        this.jpaProductOutletRepository = null;
        this.masterTreeProvider = null;
    }

    @Autowired
    public OrderMapper(UnitConversionService unitConversionService,
                       JpaProductOutletRepository jpaProductOutletRepository,
                       @Autowired(required = false) MasterTreeProvider masterTreeProvider) {
        this.unitConversionService = unitConversionService;
        this.jpaProductOutletRepository = jpaProductOutletRepository;
        this.masterTreeProvider = masterTreeProvider;
    }

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
        String displayQuantity = null;
        if (entity.getQuantity() != null && unitConversionService != null && jpaProductOutletRepository != null) {
            try {
                var prodOpt = jpaProductOutletRepository.findById(entity.getProductOutletId());
                if (prodOpt.isPresent() && prodOpt.get().getUnitMeasureId() != null) {
                    displayQuantity = unitConversionService.convertFromSmallestUnit(entity.getQuantity(), prodOpt.get().getUnitMeasureId());
                }
            } catch (Exception ignored) {}
        }
        if (displayQuantity == null && entity.getQuantity() != null) {
            displayQuantity = String.valueOf(entity.getQuantity());
        }

        return OrderItemDomain.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .productOutletId(entity.getProductOutletId())
                .productName(entity.getProductName())
                .quantity(entity.getQuantity())
                .displayQuantity(displayQuantity)
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
                .displayQuantity(domain.getDisplayQuantity())
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

        String orderStatusCode = null;
        String paymentStatusCode = null;
        if (masterTreeProvider != null) {
            MasterTree tree = masterTreeProvider.getTree();
            if (tree != null) {
                if (domain.getOrderStatusId() != null) {
                    MasterRoot node = tree.getById(domain.getOrderStatusId());
                    if (node != null && node.getShortName() != null) {
                        orderStatusCode = node.getShortName().trim();
                    }
                }
                if (domain.getPaymentStatusId() != null) {
                    MasterRoot node = tree.getById(domain.getPaymentStatusId());
                    if (node != null && node.getShortName() != null) {
                        paymentStatusCode = node.getShortName().trim();
                    }
                }
            }
        }

        return OrderResponse.builder()
                .id(domain.getId())
                .orderCode(domain.getOrderCode())
                .customerId(domain.getCustomerId())
                .outletId(domain.getOutletId())
                .orderStatusId(domain.getOrderStatusId())
                .orderStatusCode(orderStatusCode)
                .paymentStatusId(domain.getPaymentStatusId())
                .paymentStatusCode(paymentStatusCode)
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
