package org.frias.avalon.domain.sale.application.dto.response;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SaleResponse(
        Long id,
        UUID saleCode,
        BigDecimal totalAmount,
        BigDecimal amountReceived,
        BigDecimal changeGiven,
        LocalDateTime saleDate,
        MasterDataResponseDto paymentMethod,
        MasterDataResponseDto status,
        String clientFullName,
        String clientNumberid,
        Long outletId,
        Long employeeId,
        List<SaleItemResponse> items
) {
}
