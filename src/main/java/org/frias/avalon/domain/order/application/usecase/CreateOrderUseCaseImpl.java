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
import org.frias.avalon.domain.order.infrastructure.persistence.repository.JpaOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("omnichannelCreateOrderUseCaseImpl")
@RequiredArgsConstructor
public class CreateOrderUseCaseImpl implements CreateOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final JpaProductOutletRepository jpaProductOutletRepository;
    private final @org.springframework.beans.factory.annotation.Qualifier("omnichannelJpaOrderRepository") JpaOrderRepository jpaOrderRepository;
    private final @org.springframework.beans.factory.annotation.Qualifier("omnichannelOrderMapper") OrderMapper orderMapper;
    private final OrderWebSocketController orderWebSocketController;
    private final CurrentUserProviderPort currentUserProvider;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final UnitConversionService unitConversionService;

    @Override
    @Transactional
    public OrderResponse execute(CreateOrderRequest request) {
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

        String orderCode = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
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
            BigDecimal itemSubtotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);

            Integer baseQuantity = itemReq.getQuantity();
            if (productOutlet.getUnitMeasureId() != null) {
                try {
                    MasterRoot unitNode = masterTreeProvider.getTree().getById(productOutlet.getUnitMeasureId());
                    if (unitNode != null && unitNode.getShortName() != null) {
                        baseQuantity = unitConversionService.convertToSmallestUnit(BigDecimal.valueOf(itemReq.getQuantity()), unitNode.getShortName());
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

            itemsDomain.add(OrderItemDomain.builder()
                    .productOutletId(itemReq.getProductOutletId())
                    .productName(productName)
                    .quantity(baseQuantity)
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
        OrderResponse response = orderMapper.toResponse(saved);

        orderWebSocketController.broadcastOrderCreated(request.getOutletId(), response);

        return response;
    }
}
