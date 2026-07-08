package org.frias.avalon.domain.sale.application.dto.response;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        Long id,
        UUID orderCode,
        BigDecimal totalAmount,
        LocalDateTime orderDate,
        MasterDataResponseDto paymentMethod,
        MasterDataResponseDto status,
        Long outletId,
        List<OrderItemResponse> items
) {
}
