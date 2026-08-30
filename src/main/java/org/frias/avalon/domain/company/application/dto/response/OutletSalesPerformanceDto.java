package org.frias.avalon.domain.company.application.dto.response;

import java.math.BigDecimal;

/**
 * DTO representing sales performance and contribution for a single outlet.
 *
 * @param outletId            The outlet ID.
 * @param outletName          The outlet name.
 * @param totalSales          Total sales amount in the period.
 * @param transactionCount    Total completed transactions in the period.
 * @param percentageOfTotal   Contribution percentage relative to total company sales.
 */
public record OutletSalesPerformanceDto(
        Long outletId,
        String outletName,
        BigDecimal totalSales,
        Long transactionCount,
        Double percentageOfTotal
) {
}
