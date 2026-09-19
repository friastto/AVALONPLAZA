package org.frias.avalon.domain.order.application.usecase;


import org.frias.avalon.domain.order.application.dto.CreateOrderRequest;
import org.frias.avalon.domain.order.application.dto.OrderResponse;

import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderItemDomain;

import org.frias.avalon.domain.order.infrastructure.persistence.mapper.OrderMapper;
import org.frias.avalon.domain.order.presentation.controller.OrderWebSocketController;

import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.frias.avalon.core.exeptions.InsufficientStockException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.order.infrastructure.persistence.repository.JpaOrderRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("omnichannelCreateOrderUseCaseImpl")
public class CreateOrderUseCaseImpl implements CreateOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final JpaProductOutletRepository jpaProductOutletRepository;
    private final JpaOrderRepository jpaOrderRepository;
    private final OrderMapper orderMapper;
    private final OrderWebSocketController orderWebSocketController;
    private final CurrentUserProviderPort currentUserProvider;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final UnitConversionService unitConversionService;
    private final OutletRepositoryPort outletRepositoryPort;
    private final TransactionTemplate transactionTemplate;

    public CreateOrderUseCaseImpl(
            OrderRepositoryPort orderRepositoryPort,
            JpaProductOutletRepository jpaProductOutletRepository,
            @Qualifier("omnichannelJpaOrderRepository") JpaOrderRepository jpaOrderRepository,
            @Qualifier("omnichannelOrderMapper") OrderMapper orderMapper,
            OrderWebSocketController orderWebSocketController,
            CurrentUserProviderPort currentUserProvider,
            UserAvalonRepositoryPort userAvalonRepositoryPort,
            MasterTreeProvider masterTreeProvider,
            UnitConversionService unitConversionService,
            OutletRepositoryPort outletRepositoryPort,
            PlatformTransactionManager transactionManager) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.jpaProductOutletRepository = jpaProductOutletRepository;
        this.jpaOrderRepository = jpaOrderRepository;
        this.orderMapper = orderMapper;
        this.orderWebSocketController = orderWebSocketController;
        this.currentUserProvider = currentUserProvider;
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
        this.masterTreeProvider = masterTreeProvider;
        this.unitConversionService = unitConversionService;
        this.outletRepositoryPort = outletRepositoryPort;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public OrderResponse execute(CreateOrderRequest request) {
        Long previousOutletId = TenantContext.getTenantOutletId();
        Long previousTenantId = TenantContext.getTenantId();

        try {
            if (request.getOutletId() != null) {
                OutletDomain outlet = outletRepositoryPort.findById(request.getOutletId())
                        .orElseThrow(() -> new ResourceNotFoundException("Tienda con ID " + request.getOutletId() + " no encontrada"));
                if (outlet.getCompanyId() != null) {
                    TenantContext.setTenantId(outlet.getCompanyId());
                }
                TenantContext.setTenantOutletId(outlet.getId());
            }

            OrderResponse response = transactionTemplate.execute(status -> doCreateOrder(request));

            if (response != null) {
                orderWebSocketController.broadcastOrderCreated(request.getOutletId(), response);
            }

            return response;
        } finally {
            TenantContext.setTenantId(previousTenantId);
            TenantContext.setTenantOutletId(previousOutletId);
        }
    }

    private OrderResponse doCreateOrder(CreateOrderRequest request) {
        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot ordPenNode = tree.getByCode("PEN");
        if (ordPenNode == null) {
            ordPenNode = tree.getByCode("ORD_PEN");
        }
        if (ordPenNode == null) {
            throw new IllegalStateException("Estado maestro PEN no encontrado en MasterTree");
        }
        Long ordPenStatusId = ordPenNode.getId();

        MasterRoot payPenNode = tree.getByCode("PAY_PEN");
        if (payPenNode == null) {
            throw new IllegalStateException("Estado maestro PAY_PEN no encontrado en MasterTree");
        }
        Long payPenStatusId = payPenNode.getId();
        Long dispPenStatusId = ordPenStatusId;

        String orderCode = "ORD-" + UUID.randomUUID().toString().toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItemDomain> itemsDomain = new ArrayList<>();

        List<Long> activeStatusIds = new ArrayList<>();
        MasterRoot penNode = tree.getByCode("PEN");
        if (penNode != null) activeStatusIds.add(penNode.getId());
        MasterRoot proNode = tree.getByCode("PRO");
        if (proNode != null) activeStatusIds.add(proNode.getId());
        MasterRoot comNode = tree.getByCode("COM");
        if (comNode != null) activeStatusIds.add(comNode.getId());
        if (activeStatusIds.isEmpty()) {
            throw new IllegalStateException("Estados logisticos (PEN, PRO, COM) no encontrados en MasterTree");
        }

        for (var itemReq : request.getItems()) {
            ProductOutlet productOutlet = jpaProductOutletRepository.findById(itemReq.getProductOutletId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + itemReq.getProductOutletId() + " no encontrado"));
            String productName = productOutlet.getLocalName() != null ? productOutlet.getLocalName() : "Producto " + itemReq.getProductOutletId();
            BigDecimal itemSubtotal = itemReq.getUnitPrice().multiply(itemReq.getQuantity()).setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(itemSubtotal);

            Integer baseQuantity = itemReq.getQuantity().intValue();
            if (productOutlet.getUnitMeasureId() != null) {
                try {
                    MasterRoot unitNode = masterTreeProvider.getTree().getById(productOutlet.getUnitMeasureId());
                    if (unitNode != null && unitNode.getShortName() != null) {
                        baseQuantity = unitConversionService.convertToSmallestUnit(itemReq.getQuantity(), unitNode.getShortName());
                    }
                } catch (Exception e) {
                    // Fallback
                }
            }

            // Validacion atomica de stock disponible (Fisico - Apartados en curso)
            Integer currentReserved = jpaOrderRepository.sumQuantityByProductOutletIdAndStatusIn(productOutlet.getId(), activeStatusIds);
            if (currentReserved == null) currentReserved = 0;
            int physicalStock = productOutlet.getStock() != null ? productOutlet.getStock() : 0;
            int availableStock = Math.max(0, physicalStock - currentReserved);

            if (baseQuantity > availableStock) {
                String readableAvailable = unitConversionService.convertFromSmallestUnit(availableStock, productOutlet.getUnitMeasureId());
                throw new InsufficientStockException("Stock insuficiente para '" + productName + "'. Disponible para compra: " + readableAvailable);
            }

            String displayQuantity = (productOutlet.getUnitMeasureId() != null)
                    ? unitConversionService.convertFromSmallestUnit(baseQuantity, productOutlet.getUnitMeasureId())
                    : String.valueOf(baseQuantity);

            itemsDomain.add(OrderItemDomain.builder()
                    .productOutletId(itemReq.getProductOutletId())
                    .productName(productName)
                    .quantity(baseQuantity)
                    .displayQuantity(displayQuantity)
                    .unitPrice(itemReq.getUnitPrice())
                    .subtotal(itemSubtotal)
                    .dispatchStatusId(dispPenStatusId)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
        }

        // Prices already include taxes from the product catalog publication.
        // BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.19));
        // BigDecimal total = subtotal.add(tax);
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = subtotal;

        Long customerId = request.getCustomerId();
        if (customerId == null) {
            UserContext userCtx = currentUserProvider.getCurrentUserContext();
            if (userCtx != null && userCtx.username() != null) {
                Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findByUserName(userCtx.username());
                if (userOpt.isPresent()) {
                    customerId = userOpt.get().getId();
                }
            }
        }

        Long paymentMethodId = request.getPaymentMethodId();
        if (paymentMethodId == null && request.getPaymentMethodCode() != null) {
            MasterRoot pmNode = tree.getByCode(request.getPaymentMethodCode().trim());
            if (pmNode != null) {
                paymentMethodId = pmNode.getId();
            }
        }
        if (paymentMethodId == null) {
            MasterRoot defaultCashNode = tree.getByCode("EFE");
            if (defaultCashNode == null) {
                defaultCashNode = tree.getByCode("MPG_CASH");
            }
            if (defaultCashNode != null) {
                paymentMethodId = defaultCashNode.getId();
            }
        }

        String deliveryType = request.getDeliveryType() != null && !request.getDeliveryType().isBlank()
                ? request.getDeliveryType().trim() : "PICKUP";
        String deliveryAddress = request.getDeliveryAddress() != null && !request.getDeliveryAddress().isBlank()
                ? request.getDeliveryAddress().trim() : null;
        BigDecimal deliveryFee = request.getDeliveryFee() != null ? request.getDeliveryFee() : BigDecimal.ZERO;

        if ("DELIVERY".equalsIgnoreCase(deliveryType) && deliveryFee.compareTo(BigDecimal.ZERO) > 0) {
            total = total.add(deliveryFee);
        }

        OrderDomain domain = OrderDomain.builder()
                .orderCode(orderCode)
                .customerId(customerId)
                .outletId(request.getOutletId())
                .orderStatusId(ordPenStatusId)
                .paymentStatusId(payPenStatusId)
                .paymentMethodId(paymentMethodId)
                .subtotal(subtotal)
                .tax(tax)
                .total(total)
                .deliveryType(deliveryType)
                .deliveryAddress(deliveryAddress)
                .deliveryFee(deliveryFee)
                .createdAt(now)
                .updatedAt(now)
                .items(itemsDomain)
                .build();

        OrderDomain saved = orderRepositoryPort.save(domain);
        return orderMapper.toResponse(saved);
    }
}
