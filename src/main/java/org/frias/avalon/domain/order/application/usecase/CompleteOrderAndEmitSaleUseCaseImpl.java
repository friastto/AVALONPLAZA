package org.frias.avalon.domain.order.application.usecase;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderItemDomain;
import org.frias.avalon.domain.order.domain.OrderStatusHistoryDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.mapper.OrderMapper;
import org.frias.avalon.domain.order.presentation.controller.OrderWebSocketController;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompleteOrderAndEmitSaleUseCaseImpl implements CompleteOrderAndEmitSaleUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final JpaProductOutletRepository jpaProductOutletRepository;
    private final @org.springframework.beans.factory.annotation.Qualifier("omnichannelOrderMapper") OrderMapper orderMapper;
    private final OrderWebSocketController orderWebSocketController;

    @Override
    @Transactional
    public OrderResponse execute(Long orderId, Long userId) {
        OrderDomain order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido con ID " + orderId + " no encontrado"));

        Long ordDelStatusId = masterDataRepositoryPort.getIdByCode("ORD_DEL");
        if (ordDelStatusId == null) {
            ordDelStatusId = masterDataRepositoryPort.getIdByCode("COM");
        }
        if (ordDelStatusId == null) {
            ordDelStatusId = 3L;
        }

        Long payPadStatusId = masterDataRepositoryPort.getIdByCode("PAY_PAD");
        if (payPadStatusId == null) {
            payPadStatusId = 2L;
        }

        Long previousStatusId = order.getOrderStatusId();
        order.setOrderStatusId(ordDelStatusId);
        order.setPaymentStatusId(payPadStatusId);
        order.setUpdatedAt(LocalDateTime.now());

        // Descontar inventario fisico de la tienda al completar/entregar la orden
        if (order.getItems() != null) {
            for (OrderItemDomain item : order.getItems()) {
                if (item.getProductOutletId() != null && item.getQuantity() != null) {
                    jpaProductOutletRepository.findById(item.getProductOutletId()).ifPresent(productOutlet -> {
                        int currentStock = productOutlet.getStock() != null ? productOutlet.getStock() : 0;
                        int newStock = Math.max(0, currentStock - item.getQuantity());
                        productOutlet.setStock(newStock);
                        productOutlet.setUpdatedAt(LocalDateTime.now());
                        jpaProductOutletRepository.save(productOutlet);
                    });
                }
            }
        }

        OrderDomain updated = orderRepositoryPort.save(order);

        orderRepositoryPort.saveStatusHistory(OrderStatusHistoryDomain.builder()
                .orderId(orderId)
                .previousStatusId(previousStatusId)
                .newStatusId(ordDelStatusId)
                .changedByUserId(userId)
                .notes("Pedido completado y entregado por el usuario " + userId)
                .createdAt(LocalDateTime.now())
                .build());

        OrderResponse response = orderMapper.toResponse(updated);
        orderWebSocketController.broadcastOrderStatusChanged(orderId, response);
        if (updated.getOutletId() != null) {
            orderWebSocketController.broadcastOrderCreated(updated.getOutletId(), response);
        }

        return response;
    }
}
