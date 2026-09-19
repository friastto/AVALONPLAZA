package org.frias.avalon.domain.order.application.usecase;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.order.application.dto.CompleteOrderRequest;
import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderItemDomain;
import org.frias.avalon.domain.order.domain.OrderStatusHistoryDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.mapper.OrderMapper;
import org.frias.avalon.domain.order.presentation.controller.OrderWebSocketController;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.springframework.stereotype.Service;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompleteOrderAndEmitSaleUseCaseImpl implements CompleteOrderAndEmitSaleUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final JpaProductOutletRepository jpaProductOutletRepository;
    private final @Qualifier("omnichannelOrderMapper") OrderMapper orderMapper;
    private final OrderWebSocketController orderWebSocketController;

    @Override
    @Transactional
    public OrderResponse execute(Long orderId, Long userId) {
        return execute(orderId, userId, null);
    }

    @Override
    @Transactional
    public OrderResponse execute(Long orderId, Long userId, CompleteOrderRequest request) {
        OrderDomain order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido con ID " + orderId + " no encontrado"));

        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot dispNode = tree.getByCode("COM");
        if (dispNode == null) {
            dispNode = tree.getByCode("ORD_DISP");
        }
        if (dispNode == null) {
            throw new IllegalStateException("Estado maestro COM u ORD_DISP no encontrado en MasterTree");
        }
        Long ordDispStatusId = dispNode.getId();

        Long previousStatusId = order.getOrderStatusId();
        order.setOrderStatusId(ordDispStatusId);
        order.setUpdatedAt(LocalDateTime.now());

        if (request != null && request.getNotes() != null && !request.getNotes().isBlank()) {
            order.setNotes(request.getNotes().trim());
        }

        // Marcar todos los items como despachados y actualizar notas por item
        if (order.getItems() != null) {
            for (OrderItemDomain item : order.getItems()) {
                item.setDispatchStatusId(ordDispStatusId);
                if (request != null && request.getItemNotes() != null && request.getItemNotes().containsKey(item.getId())) {
                    String itemNote = request.getItemNotes().get(item.getId());
                    if (itemNote != null && !itemNote.isBlank()) {
                        item.setNotes(itemNote.trim());
                    }
                }

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

        String historyNotes = (request != null && request.getNotes() != null && !request.getNotes().isBlank())
                ? request.getNotes().trim()
                : "Pedido completado en preparacion y despachado por el usuario " + userId;

        orderRepositoryPort.saveStatusHistory(OrderStatusHistoryDomain.builder()
                .orderId(orderId)
                .previousStatusId(previousStatusId)
                .newStatusId(ordDispStatusId)
                .changedByUserId(userId)
                .notes(historyNotes)
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
