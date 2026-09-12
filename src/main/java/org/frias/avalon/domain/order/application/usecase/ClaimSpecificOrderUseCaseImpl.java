package org.frias.avalon.domain.order.application.usecase;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderStatusHistoryDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.mapper.OrderMapper;
import org.frias.avalon.domain.order.presentation.controller.OrderWebSocketController;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClaimSpecificOrderUseCaseImpl implements ClaimSpecificOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final @org.springframework.beans.factory.annotation.Qualifier("omnichannelOrderMapper") OrderMapper orderMapper;
    private final OrderWebSocketController orderWebSocketController;

    @Override
    @Transactional
    public OrderResponse execute(Long orderId, Long userId) {
        OrderDomain order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido con ID " + orderId + " no encontrado"));

        Long ordRecStatusId = masterDataRepositoryPort.getIdByCode("ORD_REC");
        if (ordRecStatusId == null) {
            ordRecStatusId = masterDataRepositoryPort.getIdByCode("PRO");
        }
        if (ordRecStatusId == null) {
            ordRecStatusId = 2L;
        }

        Long previousStatusId = order.getOrderStatusId();
        order.setOrderStatusId(ordRecStatusId);
        order.setClaimedByUserId(userId);
        order.setUpdatedAt(LocalDateTime.now());

        OrderDomain updated = orderRepositoryPort.save(order);

        orderRepositoryPort.saveStatusHistory(OrderStatusHistoryDomain.builder()
                .orderId(updated.getId())
                .previousStatusId(previousStatusId)
                .newStatusId(ordRecStatusId)
                .changedByUserId(userId)
                .notes("Pedido aceptado e iniciado en preparacion por el usuario " + userId)
                .createdAt(LocalDateTime.now())
                .build());

        OrderResponse response = orderMapper.toResponse(updated);
        orderWebSocketController.broadcastOrderStatusChanged(updated.getId(), response);
        if (updated.getOutletId() != null) {
            orderWebSocketController.broadcastOrderCreated(updated.getOutletId(), response);
        }

        return response;
    }
}
