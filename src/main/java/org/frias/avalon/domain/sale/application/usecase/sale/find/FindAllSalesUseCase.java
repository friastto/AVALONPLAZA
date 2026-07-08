package org.frias.avalon.domain.sale.application.usecase.sale.find;

import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindAllSalesUseCase {

    Page<SaleResponse> execute(Long outletId, Pageable pageable);
}
