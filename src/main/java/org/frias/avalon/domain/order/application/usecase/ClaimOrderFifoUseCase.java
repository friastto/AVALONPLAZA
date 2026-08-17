package org.frias.avalon.domain.order.application.usecase;

import org.frias.avalon.domain.order.application.dto.OrderResponse;

public interface ClaimOrderFifoUseCase {
    OrderResponse execute(Long outletId, Long userId);
}
