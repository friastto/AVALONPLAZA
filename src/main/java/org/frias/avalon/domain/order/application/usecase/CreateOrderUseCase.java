package org.frias.avalon.domain.order.application.usecase;

import org.frias.avalon.domain.order.application.dto.CreateOrderRequest;
import org.frias.avalon.domain.order.application.dto.OrderResponse;

public interface CreateOrderUseCase {
    OrderResponse execute(CreateOrderRequest request);
}
