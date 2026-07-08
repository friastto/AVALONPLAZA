package org.frias.avalon.domain.sale.application.usecase.order.invoice;

import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import java.math.BigDecimal;
import java.util.UUID;

public interface InvoiceOrderUseCase {

    SaleResponse execute(UUID orderCode, String clientNumberid, BigDecimal amountReceived);
}
