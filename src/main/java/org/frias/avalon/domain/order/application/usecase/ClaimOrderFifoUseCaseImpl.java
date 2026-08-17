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
public class ClaimOrderFifoUseCaseImpl implements ClaimOrderFifoUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final @org.springframework.beans.factory.annotation.Qualifier("omnichannelOrderMapper") OrderMapper orderMapper;
    private final OrderWebSocketController orderWebSocketController;

    @Override
    @Transactional
    public OrderResponse execute(Long outletId, Long userId) {
        Long ordPenStatusId = masterDataRepositoryPort.getIdByCode("ORD_PEN");
        if (ordPenStatusId == null) {
            ordPenStatusId = masterDataRepositoryPort.getIdByCode("PEN");
        }
        if (ordPenStatusId == null) {
            ordPenStatusId = 1L;
        }

        OrderDomain pendingOrder = orderRepositoryPort.findNextPendingOrderFifo(outletId, ordPenStatusId)
                .orElseThrow(() -> new ResourceNotFoundException("No hay pedidos pendientes en cola FIFO para el outlet " + outletId));

        Long ordRecStatusId = masterDataRepositoryPort.getIdByCode("ORD_REC");
        if (ordRecStatusId == null) {
            ordRecStatusId = masterDataRepositoryPort.getIdByCode("PRO");
        }
        if (ordRecStatusId == null) {
            ordRecStatusId = 2L;
        }

        Long previousStatusId = pendingOrder.getOrderStatusId();
        pendingOrder.setOrderStatusId(ordRecStatusId);
        pendingOrder.setClaimedByUserId(userId);
        pendingOrder.setUpdatedAt(LocalDateTime.now());

        OrderDomain updated = orderRepositoryPort.save(pendingOrder);

        orderRepositoryPort.saveStatusHistory(OrderStatusHistoryDomain.builder()
                .orderId(updated.getId())
                .previousStatusId(previousStatusId)
                .newStatusId(ordRecStatusId)
                .changedByUserId(userId)
                .notes("Pedido asignado en cola FIFO al usuario " + userId)
                .createdAt(LocalDateTime.now())
                .build());

        OrderResponse response = orderMapper.toResponse(updated);
        orderWebSocketController.broadcastOrderStatusChanged(updated.getId(), response);

        return response;
    }
}
