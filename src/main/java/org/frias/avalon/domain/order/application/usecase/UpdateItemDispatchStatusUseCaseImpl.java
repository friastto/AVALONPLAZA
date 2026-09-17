package org.frias.avalon.domain.order.application.usecase;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderItemDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.mapper.OrderMapper;
import org.frias.avalon.domain.order.presentation.controller.OrderWebSocketController;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateItemDispatchStatusUseCaseImpl implements UpdateItemDispatchStatusUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final @org.springframework.beans.factory.annotation.Qualifier("omnichannelOrderMapper") OrderMapper orderMapper;
    private final OrderWebSocketController orderWebSocketController;
    private final MasterTreeProvider masterTreeProvider;

    @Override
    @Transactional
    public OrderResponse execute(Long orderId, Long itemId, Long statusId) {
        return execute(orderId, itemId, statusId, null);
    }

    @Override
    @Transactional
    public OrderResponse execute(Long orderId, Long itemId, Long statusId, String statusCode) {
        OrderDomain order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido con ID " + orderId + " no encontrado"));

        OrderItemDomain item = orderRepositoryPort.findItemById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item de pedido con ID " + itemId + " no encontrado"));

        Long targetStatusId = statusId;
        if (targetStatusId == null && statusCode != null && masterTreeProvider != null) {
            MasterTree tree = masterTreeProvider.getTree();
            if (tree != null) {
                MasterRoot node = tree.getByCode(statusCode.trim());
                if (node != null) {
                    targetStatusId = node.getId();
                }
            }
        }

        if (targetStatusId == null) {
            throw new DomainValidationException("Debe proporcionar un statusId valido o un statusCode existente");
        }

        item.setDispatchStatusId(targetStatusId);
        item.setUpdatedAt(LocalDateTime.now());
        orderRepositoryPort.saveItem(item);

        OrderDomain refreshedOrder = orderRepositoryPort.findById(orderId)
                .orElse(order);

        OrderResponse response = orderMapper.toResponse(refreshedOrder);
        orderWebSocketController.broadcastOrderStatusChanged(orderId, response);
        if (refreshedOrder.getOutletId() != null) {
            orderWebSocketController.broadcastOrderCreated(refreshedOrder.getOutletId(), response);
        }

        return response;
    }
}
