package org.frias.avalon.domain.order.application.usecase;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderItemDomain;
import org.frias.avalon.domain.order.domain.OrderStatusHistoryDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.mapper.OrderMapper;
import org.frias.avalon.domain.order.presentation.controller.OrderWebSocketController;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

@Service
public class DeliverOrderByQrUseCaseImpl implements DeliverOrderByQrUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final JpaProductOutletRepository jpaProductOutletRepository;
    private final OrderMapper orderMapper;
    private final OrderWebSocketController orderWebSocketController;
    private final OutletRepositoryPort outletRepositoryPort;
    private final TransactionTemplate transactionTemplate;

    public DeliverOrderByQrUseCaseImpl(
            OrderRepositoryPort orderRepositoryPort,
            MasterDataRepositoryPort masterDataRepositoryPort,
            JpaProductOutletRepository jpaProductOutletRepository,
            @Qualifier("omnichannelOrderMapper") OrderMapper orderMapper,
            OrderWebSocketController orderWebSocketController,
            OutletRepositoryPort outletRepositoryPort,
            PlatformTransactionManager transactionManager) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.masterDataRepositoryPort = masterDataRepositoryPort;
        this.jpaProductOutletRepository = jpaProductOutletRepository;
        this.orderMapper = orderMapper;
        this.orderWebSocketController = orderWebSocketController;
        this.outletRepositoryPort = outletRepositoryPort;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public OrderResponse execute(String orderCode, Long userId) {
        OrderDomain order = orderRepositoryPort.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido con codigo " + orderCode + " no encontrado"));

        Long ordEntStatusId = masterDataRepositoryPort.getIdByCode("ORD_ENT");
        if (ordEntStatusId == null) {
            ordEntStatusId = masterDataRepositoryPort.getIdByCode("ENT");
        }
        if (ordEntStatusId == null) {
            ordEntStatusId = 4L;
        }

        if (ordEntStatusId.equals(order.getOrderStatusId()) || Long.valueOf(103L).equals(order.getOrderStatusId())) {
            throw new IllegalStateException("El pedido con codigo " + orderCode + " ya fue entregado previamente");
        }

        if (Long.valueOf(17L).equals(order.getOrderStatusId()) || Long.valueOf(18L).equals(order.getOrderStatusId())) {
            throw new IllegalStateException("El pedido con codigo " + orderCode + " se encuentra cancelado o rechazado");
        }

        Long previousOutletId = TenantContext.getTenantOutletId();
        Long previousTenantId = TenantContext.getTenantId();

        try {
            if (order.getOutletId() != null) {
                OutletDomain outlet = outletRepositoryPort.findById(order.getOutletId())
                        .orElseThrow(() -> new ResourceNotFoundException("Tienda con ID " + order.getOutletId() + " no encontrada"));
                if (outlet.getCompanyId() != null) {
                    TenantContext.setTenantId(outlet.getCompanyId());
                }
                TenantContext.setTenantOutletId(outlet.getId());
            }

            final Long finalEntStatusId = ordEntStatusId;
            OrderResponse response = transactionTemplate.execute(status -> doDeliverOrder(order, userId, finalEntStatusId));

            if (response != null) {
                orderWebSocketController.broadcastOrderStatusChanged(order.getId(), response);
                if (order.getOutletId() != null) {
                    orderWebSocketController.broadcastOrderCreated(order.getOutletId(), response);
                }
            }

            return response;
        } finally {
            TenantContext.setTenantId(previousTenantId);
            TenantContext.setTenantOutletId(previousOutletId);
        }
    }

    private OrderResponse doDeliverOrder(OrderDomain order, Long userId, Long entStatusId) {
        Long payPadStatusId = masterDataRepositoryPort.getIdByCode("PAY_PAD");
        if (payPadStatusId == null) {
            payPadStatusId = 2L;
        }

        Long previousStatusId = order.getOrderStatusId();
        order.setOrderStatusId(entStatusId);
        order.setPaymentStatusId(payPadStatusId);
        order.setUpdatedAt(LocalDateTime.now());

        // Deduccion del inventario fisico en la tienda
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
                .orderId(order.getId())
                .previousStatusId(previousStatusId)
                .newStatusId(entStatusId)
                .changedByUserId(userId)
                .notes("Pedido entregado y despachado mediante escaneo de codigo QR por el usuario " + userId)
                .createdAt(LocalDateTime.now())
                .build());

        return orderMapper.toResponse(updated);
    }
}
