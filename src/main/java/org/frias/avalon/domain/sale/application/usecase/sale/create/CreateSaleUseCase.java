package org.frias.avalon.domain.sale.application.usecase.sale.create;

import org.frias.avalon.domain.sale.application.dto.request.CreateSaleRequest;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;

public interface CreateSaleUseCase {

    SaleResponse execute(CreateSaleRequest request);
}
