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

import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCaseImpl implements CreateOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final JpaProductOutletRepository jpaProductOutletRepository;
    private final OrderMapper orderMapper;
    private final OrderWebSocketController orderWebSocketController;

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

        for (var itemReq : request.getItems()) {
            ProductOutlet productOutlet = jpaProductOutletRepository.findById(itemReq.getProductOutletId())
                    .orElse(null);
            String productName = productOutlet != null ? productOutlet.getLocalName() : "Producto " + itemReq.getProductOutletId();
            BigDecimal itemSubtotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);

            itemsDomain.add(OrderItemDomain.builder()
                    .productOutletId(itemReq.getProductOutletId())
                    .productName(productName)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .subtotal(itemSubtotal)
                    .dispatchStatusId(dispPenStatusId)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
        }

        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.19));
        BigDecimal total = subtotal.add(tax);

        OrderDomain domain = OrderDomain.builder()
                .orderCode(orderCode)
                .customerId(request.getCustomerId())
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
