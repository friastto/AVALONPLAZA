package org.frias.avalon.domain.order.application.usecase;

import org.frias.avalon.domain.order.application.dto.OrderResponse;

public interface ClaimSpecificOrderUseCase {
    OrderResponse execute(Long orderId, Long userId);
}
