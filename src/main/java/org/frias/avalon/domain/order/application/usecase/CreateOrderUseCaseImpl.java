package org.frias.avalon.domain.order.application.usecase;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
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
    private final MasterDataRepositoryPort masterDataRepositoryPort;
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
            MasterDataRepositoryPort masterDataRepositoryPort,
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
        this.masterDataRepositoryPort = masterDataRepositoryPort;
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
        Long ordPenStatusId = masterDataRepositoryPort.getIdByCode("ORD_PEN");
        if (ordPenStatusId == null) {
            ordPenStatusId = masterDataRepositoryPort.getIdByCode("PEN");
        }
        if (ordPenStatusId == null) {
            ordPenStatusId = 1L;
        }

        Long payPenStatusId = masterDataRepositoryPort.getIdByCode("PAY_PEN");
        if (payPenStatusId == null) {
            payPenStatusId = 1L;
        }

        Long dispPenStatusId = masterDataRepositoryPort.getIdByCode("PEN");
        if (dispPenStatusId == null) {
            dispPenStatusId = 1L;
        }

        String orderCode = "ORD-" + UUID.randomUUID().toString().toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItemDomain> itemsDomain = new ArrayList<>();

        List<Long> activeStatusIds = new ArrayList<>();
        Long penId = masterDataRepositoryPort.getIdByCode("PEN");
        if (penId != null) activeStatusIds.add(penId);
        Long proId = masterDataRepositoryPort.getIdByCode("PRO");
        if (proId != null) activeStatusIds.add(proId);
        Long comId = masterDataRepositoryPort.getIdByCode("COM");
        if (comId != null) activeStatusIds.add(comId);
        if (activeStatusIds.isEmpty()) {
            activeStatusIds = List.of(14L, 15L, 16L);
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

        OrderDomain domain = OrderDomain.builder()
                .orderCode(orderCode)
                .customerId(customerId)
                .outletId(request.getOutletId())
                .orderStatusId(ordPenStatusId)
                .paymentStatusId(payPenStatusId)
                .paymentMethodId(request.getPaymentMethodId())
                .subtotal(subtotal)
                .tax(tax)
                .total(total)
                .createdAt(now)
                .updatedAt(now)
                .items(itemsDomain)
                .build();

        OrderDomain saved = orderRepositoryPort.save(domain);
        return orderMapper.toResponse(saved);
    }
}
