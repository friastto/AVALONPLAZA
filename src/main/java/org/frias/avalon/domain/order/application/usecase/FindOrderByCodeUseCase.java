package org.frias.avalon.domain.order.application.usecase;

import org.frias.avalon.domain.order.application.dto.OrderResponse;

public interface FindOrderByCodeUseCase {
    OrderResponse execute(String orderCode);
}
