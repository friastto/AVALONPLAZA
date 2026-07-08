package org.frias.avalon.domain.sale.application.usecase.order.find;

import org.frias.avalon.domain.sale.application.dto.response.OrderResponse;
import java.util.UUID;

public interface FindOrderByCodeUseCase {

    OrderResponse execute(UUID code);
}
