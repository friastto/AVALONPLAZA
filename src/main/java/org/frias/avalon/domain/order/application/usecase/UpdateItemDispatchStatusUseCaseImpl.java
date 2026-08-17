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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateItemDispatchStatusUseCaseImpl implements UpdateItemDispatchStatusUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderMapper orderMapper;
    private final OrderWebSocketController orderWebSocketController;

    @Override
    @Transactional
    public OrderResponse execute(Long orderId, Long itemId, Long statusId) {
        OrderDomain order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido con ID " + orderId + " no encontrado"));

        OrderItemDomain item = orderRepositoryPort.findItemById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item de pedido con ID " + itemId + " no encontrado"));

        item.setDispatchStatusId(statusId);
        item.setUpdatedAt(LocalDateTime.now());
        orderRepositoryPort.saveItem(item);

        OrderDomain refreshedOrder = orderRepositoryPort.findById(orderId)
                .orElse(order);

        OrderResponse response = orderMapper.toResponse(refreshedOrder);
        orderWebSocketController.broadcastOrderStatusChanged(orderId, response);

        return response;
    }
}
