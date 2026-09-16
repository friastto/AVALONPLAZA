package org.frias.avalon.domain.company.application.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Enterprise Dashboard Response DTO for Level 2 Company financial and operational metrics.
 *
 * @param companyId               Unique company ID.
 * @param companyName             Name of the company.
 * @param period                  Active temporal filter ('HOY', 'MES', 'ANIO', 'HISTORICO').
 * @param selectedOutletId        Specific outlet ID if filtered, or null for company-wide consolidated view.
 * @param totalSales              Gross revenue from completed sales.
 * @param totalExpenses           Expenses, product returns, and cancellations.
 * @param netProfit               Net profit calculated as totalSales - totalExpenses.
 * @param profitMarginPercentage  Profit margin percentage relative to gross sales.
 * @param transactionCount        Total number of processed sales.
 * @param averageTicket           Average revenue per sale transaction.
 * @param salesByPaymentMethod    Breakdown of sales grouped by payment method code (e.g. EFE, FIA).
 * @param outletSales             Performance and sales breakdown per linked outlet.
 * @param consolidatedCash        Total audited and secured cash from CLOSED cash sessions.
 * @param inFlightCash            Estimated cash currently in transit across OPEN cash sessions.
 * @param closedSessionsCount     Number of closed and audited cash sessions.
 * @param openSessionsCount       Number of active / open cash sessions.
 */
public record CompanyDashboardResponse(
        Long companyId,
        String companyName,
        String period,
        Long selectedOutletId,
        BigDecimal totalSales,
        BigDecimal totalExpenses,
        BigDecimal netProfit,
        Double profitMarginPercentage,
        Long transactionCount,
        BigDecimal averageTicket,
        Map<String, BigDecimal> salesByPaymentMethod,
        List<OutletSalesPerformanceDto> outletSales,
        BigDecimal consolidatedCash,
        BigDecimal inFlightCash,
        int closedSessionsCount,
        int openSessionsCount
) {
}
