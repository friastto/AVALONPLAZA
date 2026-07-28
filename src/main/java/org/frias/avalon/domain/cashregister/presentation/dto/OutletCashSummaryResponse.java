package org.frias.avalon.domain.cashregister.presentation.dto;

import lombok.Builder;
import lombok.Data;
import org.frias.avalon.domain.cashregister.domain.OutletCashSummaryDomain;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OutletCashSummaryResponse {

    private Long outletId;
    private BigDecimal totalCashSales;
    private BigDecimal totalDigitalSales;
    private BigDecimal totalCardSales;
    private BigDecimal totalCreditSales;
    private BigDecimal totalExpenses;
    private BigDecimal currentExpectedCashInStore;
    private int activeSessionsCount;
    private int closedSessionsCount;
    private List<CashSessionResponse> activeSessions;
    private BigDecimal totalPickups;
    private BigDecimal cashThresholdAmount;
    private Boolean thresholdExceeded;

    public static OutletCashSummaryResponse fromDomain(OutletCashSummaryDomain domain) {
        if (domain == null) return null;
        List<CashSessionResponse> sessionResponses = domain.getActiveSessions() != null
                ? domain.getActiveSessions().stream().map(CashSessionResponse::fromDomain).toList()
                : List.of();

        return OutletCashSummaryResponse.builder()
                .outletId(domain.getOutletId())
                .totalCashSales(domain.getTotalCashSales())
                .totalDigitalSales(domain.getTotalDigitalSales())
                .totalCardSales(domain.getTotalCardSales())
                .totalCreditSales(domain.getTotalCreditSales())
                .totalExpenses(domain.getTotalExpenses())
                .currentExpectedCashInStore(domain.getCurrentExpectedCashInStore())
                .activeSessionsCount(domain.getActiveSessionsCount())
                .closedSessionsCount(domain.getClosedSessionsCount())
                .activeSessions(sessionResponses)
                .totalPickups(domain.getTotalPickups())
                .cashThresholdAmount(domain.getCashThresholdAmount())
                .thresholdExceeded(domain.getThresholdExceeded())
                .build();
    }
}
