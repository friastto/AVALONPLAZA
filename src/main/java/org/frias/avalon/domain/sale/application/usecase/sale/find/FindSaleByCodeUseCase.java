package org.frias.avalon.domain.sale.application.usecase.sale.find;

import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import java.util.UUID;

public interface FindSaleByCodeUseCase {

    SaleResponse execute(UUID code);
}
