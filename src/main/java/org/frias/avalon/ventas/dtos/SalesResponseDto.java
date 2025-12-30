package org.frias.avalon.ventas.dtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SalesResponseDto(

        Long id,

        UUID saleCode,

        BigDecimal total,

        BigDecimal amountReceived,

        BigDecimal amountReturned,

        String paymentMethod,

        String enployeeName,

        String customerId,

        String status,

        List<SaleDetailResponseDto> detail
) {
}
