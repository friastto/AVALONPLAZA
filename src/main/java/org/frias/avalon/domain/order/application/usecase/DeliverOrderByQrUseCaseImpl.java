package org.frias.avalon.domain.order.application.usecase;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.TenantContext;
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
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.sale.domain.SaleItemDomain;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DeliverOrderByQrUseCaseImpl implements DeliverOrderByQrUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final JpaProductOutletRepository jpaProductOutletRepository;
    private final SaleRepositoryPort saleRepositoryPort;
    private final OrderMapper orderMapper;
    private final OrderWebSocketController orderWebSocketController;
    private final OutletRepositoryPort outletRepositoryPort;
    private final TransactionTemplate transactionTemplate;

    public DeliverOrderByQrUseCaseImpl(
            OrderRepositoryPort orderRepositoryPort,
            MasterTreeProvider masterTreeProvider,
            JpaProductOutletRepository jpaProductOutletRepository,
            SaleRepositoryPort saleRepositoryPort,
            @Qualifier("omnichannelOrderMapper") OrderMapper orderMapper,
            OrderWebSocketController orderWebSocketController,
            OutletRepositoryPort outletRepositoryPort,
            PlatformTransactionManager transactionManager) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.masterTreeProvider = masterTreeProvider;
        this.jpaProductOutletRepository = jpaProductOutletRepository;
        this.saleRepositoryPort = saleRepositoryPort;
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

        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot entNode = tree.getByCode("ENT");
        if (entNode == null) {
            entNode = tree.getByCode("ORD_DEL");
        }
        if (entNode == null) {
            throw new IllegalStateException("Estado maestro ENT no encontrado en MasterTree");
        }
        Long ordEntStatusId = entNode.getId();

        MasterRoot currentNode = tree.getById(order.getOrderStatusId());
        if (tree.is(currentNode, "ORD_DEL") || tree.is(currentNode, "ENT")) {
            throw new IllegalStateException("El pedido con codigo " + orderCode + " ya fue entregado y cobrado previamente");
        }

        if (tree.is(currentNode, "ORD_CAN") || tree.is(currentNode, "CAN") || tree.is(currentNode, "REC")) {
            throw new IllegalStateException("El pedido con codigo " + orderCode + " se encuentra cancelado o rechazado");
        }

        Long previousOutletId = TenantContext.getTenantOutletId();
        Long previousTenantId = TenantContext.getTenantId();

        if (previousOutletId != null && order.getOutletId() != null && !previousOutletId.equals(order.getOutletId())) {
            throw new IllegalStateException("Acceso denegado: El pedido pertenece a la tienda #" + order.getOutletId() + " y tu sesion pertenece a la tienda #" + previousOutletId);
        }

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
        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot padNode = tree.getByCode("PAY_PAD");
        if (padNode == null) {
            throw new IllegalStateException("Estado maestro PAY_PAD no encontrado en MasterTree");
        }
        Long payPadStatusId = padNode.getId();

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

        // Emision de Venta oficial (Sale) asociada al cobro en efectivo y al cajero
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            List<SaleItemDomain> saleItems = new ArrayList<>();
            for (OrderItemDomain item : order.getItems()) {
                Long unitMeasureId = 1L;
                if (item.getProductOutletId() != null) {
                    Optional<ProductOutlet> poOpt = jpaProductOutletRepository.findById(item.getProductOutletId());
                    if (poOpt.isPresent() && poOpt.get().getUnitMeasureId() != null) {
                        unitMeasureId = poOpt.get().getUnitMeasureId();
                    }
                }
                Integer qty = item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1;
                BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal subtotal = item.getSubtotal() != null ? item.getSubtotal() : unitPrice.multiply(BigDecimal.valueOf(qty));

                SaleItemDomain saleItem = new SaleItemDomain(
                        null,
                        item.getProductOutletId() != null ? item.getProductOutletId() : 1L,
                        qty,
                        item.getDisplayQuantity() != null ? item.getDisplayQuantity() : (qty + " UND"),
                        unitPrice,
                        subtotal,
                        unitMeasureId
                );
                saleItems.add(saleItem);
            }

            MasterRoot actNode = tree.getByCode("ACT");
            if (actNode == null) {
                throw new IllegalStateException("Estado maestro ACT no encontrado en MasterTree");
            }
            Long activeSaleStatusId = actNode.getId();

            MasterRoot cashNode = tree.getByCode("CASH");
            if (cashNode == null) {
                cashNode = tree.getByCode("EFECTIVO");
            }
            Long cashPaymentMethodId = cashNode != null ? cashNode.getId() : order.getPaymentMethodId();

            SaleDomain sale = SaleDomain.create(
                    cashPaymentMethodId,
                    activeSaleStatusId,
                    order.getCustomerId() != null && order.getCustomerId() > 0 ? order.getCustomerId() : 1L,
                    order.getOutletId() != null ? order.getOutletId() : 1L,
                    userId != null && userId > 0 ? userId : 1L,
                    saleItems
            );
            sale.applyPayment(sale.getTotalAmount(), false);
            saleRepositoryPort.save(sale);
        }

        return orderMapper.toResponse(updated);
    }
}
