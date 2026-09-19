package org.frias.avalon.domain.order.infrastructure.persistence.mapper;

import org.frias.avalon.domain.order.application.dto.OrderItemResponse;
import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderItemDomain;
import org.frias.avalon.domain.order.domain.OrderStatusHistoryDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderEntity;
import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderItemEntity;
import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderStatusHistoryEntity;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.frias.avalon.domain.user.infraestructure.persistence.repository.JpaUserAvalonRepository;
import org.frias.avalon.domain.user.infraestructure.persistence.entity.UserAvalon;
import org.frias.avalon.domain.person.infraestructure.persistence.repository.JpaPersonRepository;
import org.frias.avalon.domain.person.infraestructure.persistence.entity.PersonEntity;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("omnichannelOrderMapper")
public class OrderMapper {

    private final UnitConversionService unitConversionService;
    private final JpaProductOutletRepository jpaProductOutletRepository;
    private final MasterTreeProvider masterTreeProvider;
    private final JpaUserAvalonRepository jpaUserAvalonRepository;
    private final JpaPersonRepository jpaPersonRepository;
    private final JpaOutletRepository jpaOutletRepository;

    public OrderMapper() {
        this.unitConversionService = null;
        this.jpaProductOutletRepository = null;
        this.masterTreeProvider = null;
        this.jpaUserAvalonRepository = null;
        this.jpaPersonRepository = null;
        this.jpaOutletRepository = null;
    }

    @Autowired
    public OrderMapper(UnitConversionService unitConversionService,
                       JpaProductOutletRepository jpaProductOutletRepository,
                       @Autowired(required = false) MasterTreeProvider masterTreeProvider,
                       @Autowired(required = false) JpaUserAvalonRepository jpaUserAvalonRepository,
                       @Autowired(required = false) JpaPersonRepository jpaPersonRepository,
                       @Autowired(required = false) JpaOutletRepository jpaOutletRepository) {
        this.unitConversionService = unitConversionService;
        this.jpaProductOutletRepository = jpaProductOutletRepository;
        this.masterTreeProvider = masterTreeProvider;
        this.jpaUserAvalonRepository = jpaUserAvalonRepository;
        this.jpaPersonRepository = jpaPersonRepository;
        this.jpaOutletRepository = jpaOutletRepository;
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
                .deliveryType(entity.getDeliveryType())
                .deliveryAddress(entity.getDeliveryAddress())
                .deliveryFee(entity.getDeliveryFee())
                .notes(entity.getNotes())
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
                .deliveryType(domain.getDeliveryType())
                .deliveryAddress(domain.getDeliveryAddress())
                .deliveryFee(domain.getDeliveryFee())
                .notes(domain.getNotes())
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
                .notes(entity.getNotes())
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
                .notes(domain.getNotes())
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
        MasterRefDto dispatchStatus = null;
        if (masterTreeProvider != null && domain.getDispatchStatusId() != null) {
            MasterTree tree = masterTreeProvider.getTree();
            if (tree != null) {
                dispatchStatus = MasterRefDto.from(tree.getById(domain.getDispatchStatusId()));
            }
        }
        return OrderItemResponse.builder()
                .id(domain.getId())
                .productOutletId(domain.getProductOutletId())
                .productName(domain.getProductName())
                .quantity(domain.getQuantity())
                .displayQuantity(domain.getDisplayQuantity())
                .unitPrice(domain.getUnitPrice())
                .subtotal(domain.getSubtotal())
                .dispatchStatusId(domain.getDispatchStatusId())
                .dispatchStatus(dispatchStatus)
                .notes(domain.getNotes())
                .build();
    }

    public OrderResponse toResponse(OrderDomain domain) {
        if (domain == null) return null;
        List<OrderItemResponse> itemResponses = domain.getItems() != null
                ? domain.getItems().stream().map(this::toItemResponse).collect(Collectors.toList())
                : List.of();

        String orderStatusCode = null;
        String paymentStatusCode = null;
        MasterRefDto orderStatus = null;
        MasterRefDto paymentStatus = null;
        MasterRefDto paymentMethod = null;

        if (masterTreeProvider != null) {
            MasterTree tree = masterTreeProvider.getTree();
            if (tree != null) {
                if (domain.getOrderStatusId() != null) {
                    MasterRoot node = tree.getById(domain.getOrderStatusId());
                    if (node != null) {
                        orderStatus = MasterRefDto.from(node);
                        if (node.getShortName() != null) {
                            orderStatusCode = node.getShortName().trim();
                        }
                    }
                }
                if (domain.getPaymentStatusId() != null) {
                    MasterRoot node = tree.getById(domain.getPaymentStatusId());
                    if (node != null) {
                        paymentStatus = MasterRefDto.from(node);
                        if (node.getShortName() != null) {
                            paymentStatusCode = node.getShortName().trim();
                        }
                    }
                }
                if (domain.getPaymentMethodId() != null) {
                    MasterRoot node = tree.getById(domain.getPaymentMethodId());
                    if (node != null) {
                        paymentMethod = MasterRefDto.from(node);
                    }
                }
            }
        }

        String claimedByName = null;
        String claimedByUserName = null;
        if (domain.getClaimedByUserId() != null && jpaUserAvalonRepository != null) {
            Optional<UserAvalon> userOpt = jpaUserAvalonRepository.findById(domain.getClaimedByUserId());
            if (userOpt.isPresent()) {
                UserAvalon user = userOpt.get();
                claimedByUserName = user.getUserName();
                if (user.getPersonId() != null && jpaPersonRepository != null) {
                    Optional<PersonEntity> personOpt = jpaPersonRepository.findById(user.getPersonId());
                    if (personOpt.isPresent()) {
                        PersonEntity person = personOpt.get();
                        String fullName = ((person.getName() != null ? person.getName().trim() : "") + " " +
                                (person.getLastName() != null ? person.getLastName().trim() : "")).trim();
                        claimedByName = !fullName.isEmpty() ? fullName : claimedByUserName;
                    } else {
                        claimedByName = claimedByUserName;
                    }
                } else {
                    claimedByName = claimedByUserName;
                }
            }
        }

        String outletName = null;
        if (domain.getOutletId() != null && jpaOutletRepository != null) {
            outletName = jpaOutletRepository.findById(domain.getOutletId())
                    .map(Outlet::getName)
                    .orElse(null);
        }

        return OrderResponse.builder()
                .id(domain.getId())
                .orderCode(domain.getOrderCode())
                .customerId(domain.getCustomerId())
                .outletId(domain.getOutletId())
                .outletName(outletName)
                .orderStatusId(domain.getOrderStatusId())
                .orderStatusCode(orderStatusCode)
                .orderStatus(orderStatus)
                .paymentStatusId(domain.getPaymentStatusId())
                .paymentStatusCode(paymentStatusCode)
                .paymentStatus(paymentStatus)
                .paymentMethodId(domain.getPaymentMethodId())
                .paymentMethod(paymentMethod)
                .subtotal(domain.getSubtotal())
                .tax(domain.getTax())
                .total(domain.getTotal())
                .claimedByUserId(domain.getClaimedByUserId())
                .claimedByName(claimedByName)
                .claimedByUserName(claimedByUserName)
                .deliveryType(domain.getDeliveryType())
                .deliveryAddress(domain.getDeliveryAddress())
                .deliveryFee(domain.getDeliveryFee())
                .notes(domain.getNotes())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .items(itemResponses)
                .build();
    }
}
