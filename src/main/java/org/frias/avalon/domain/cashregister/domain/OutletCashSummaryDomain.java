package org.frias.avalon.domain.cashregister.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Objeto de valor para el resumen consolidado de la tienda (Outlet) enfocado en el Administrador.
 */
public class OutletCashSummaryDomain {

    private final Long outletId;
    private final BigDecimal totalCashSales;
    private final BigDecimal totalDigitalSales;
    private final BigDecimal totalCardSales;
    private final BigDecimal totalCreditSales;
    private final BigDecimal totalExpenses;
    private final BigDecimal currentExpectedCashInStore;
    private final int activeSessionsCount;
    private final int closedSessionsCount;
    private final List<CashSessionDomain> activeSessions;

    public OutletCashSummaryDomain(
            Long outletId,
            BigDecimal totalCashSales,
            BigDecimal totalDigitalSales,
            BigDecimal totalCardSales,
            BigDecimal totalCreditSales,
            BigDecimal totalExpenses,
            BigDecimal currentExpectedCashInStore,
            int activeSessionsCount,
            int closedSessionsCount,
            List<CashSessionDomain> activeSessions
    ) {
        this.outletId = outletId;
        this.totalCashSales = totalCashSales != null ? totalCashSales : BigDecimal.ZERO;
        this.totalDigitalSales = totalDigitalSales != null ? totalDigitalSales : BigDecimal.ZERO;
        this.totalCardSales = totalCardSales != null ? totalCardSales : BigDecimal.ZERO;
        this.totalCreditSales = totalCreditSales != null ? totalCreditSales : BigDecimal.ZERO;
        this.totalExpenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
        this.currentExpectedCashInStore = currentExpectedCashInStore != null ? currentExpectedCashInStore : BigDecimal.ZERO;
        this.activeSessionsCount = activeSessionsCount;
        this.closedSessionsCount = closedSessionsCount;
        this.activeSessions = activeSessions != null ? activeSessions : List.of();
    }

    public Long getOutletId() {
        return outletId;
    }

    public BigDecimal getTotalCashSales() {
        return totalCashSales;
    }

    public BigDecimal getTotalDigitalSales() {
        return totalDigitalSales;
    }

    public BigDecimal getTotalCardSales() {
        return totalCardSales;
    }

    public BigDecimal getTotalCreditSales() {
        return totalCreditSales;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public BigDecimal getCurrentExpectedCashInStore() {
        return currentExpectedCashInStore;
    }

    public int getActiveSessionsCount() {
        return activeSessionsCount;
    }

    public int getClosedSessionsCount() {
        return closedSessionsCount;
    }

    public List<CashSessionDomain> getActiveSessions() {
        return activeSessions;
    }
}
