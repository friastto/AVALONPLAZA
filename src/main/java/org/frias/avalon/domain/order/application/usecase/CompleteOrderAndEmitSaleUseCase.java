package org.frias.avalon.domain.order.application.usecase;

import org.frias.avalon.domain.order.application.dto.OrderResponse;

public interface CompleteOrderAndEmitSaleUseCase {
    OrderResponse execute(Long orderId, Long userId);
}
