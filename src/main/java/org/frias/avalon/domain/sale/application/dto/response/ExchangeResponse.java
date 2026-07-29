package org.frias.avalon.domain.sale.application.dto.response;

import java.math.BigDecimal;

public record ExchangeResponse(
        ReturnResponse returnDetail,
        SaleResponse newSaleDetail,
        BigDecimal totalReturned,
        BigDecimal totalNewItems,
        BigDecimal netDifference, // >0 excedente a pagar, <0 saldo a favor, 0 mano a mano
        String paymentStatusMessage
) {}
