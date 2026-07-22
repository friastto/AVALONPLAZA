package org.frias.avalon.domain.sale.application.usecase.sale.returns;

import org.frias.avalon.domain.sale.application.dto.request.CreateExchangeRequest;
import org.frias.avalon.domain.sale.application.dto.response.ExchangeResponse;

public interface CreateExchangeUseCase {
    ExchangeResponse execute(CreateExchangeRequest request);
}
