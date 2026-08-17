package org.frias.avalon.domain.order.application.usecase;

import org.frias.avalon.domain.order.application.dto.OrderResponse;

public interface UpdateItemDispatchStatusUseCase {
    OrderResponse execute(Long orderId, Long itemId, Long statusId);
}
