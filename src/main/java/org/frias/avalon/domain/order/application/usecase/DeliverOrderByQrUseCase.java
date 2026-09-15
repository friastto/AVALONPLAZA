package org.frias.avalon.domain.order.application.usecase;

import org.frias.avalon.domain.order.application.dto.OrderResponse;

public interface DeliverOrderByQrUseCase {
    OrderResponse execute(String orderCode, Long userId);
}
