package org.frias.avalon.domain.sale.application.usecase.order.create;

import org.frias.avalon.domain.sale.application.dto.request.CreateOrderRequest;
import org.frias.avalon.domain.sale.application.dto.response.OrderResponse;

public interface CreateOrderUseCase {

    OrderResponse execute(CreateOrderRequest request);
}
