package org.frias.avalon.domain.sale.application.dto.response;

import java.math.BigDecimal;

public record ReturnItemResponse(
        Long productId,
        String productName,
        String displayQuantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
