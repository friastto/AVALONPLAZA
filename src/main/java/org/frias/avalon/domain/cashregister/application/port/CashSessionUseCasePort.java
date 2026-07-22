package org.frias.avalon.domain.cashregister.application.port;

import org.frias.avalon.domain.cashregister.domain.CashExpenseDomain;
import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;
import org.frias.avalon.domain.cashregister.domain.OutletCashSummaryDomain;

import java.math.BigDecimal;

public interface CashSessionUseCasePort {

    CashSessionDomain openSession(Long outletId, Long employeeId, BigDecimal initialBase);

    CashSessionDomain closeSession(Long sessionId, BigDecimal actualCashContado, String notes);

    CashExpenseDomain registerExpense(Long sessionId, BigDecimal amount, String reason, Long registeredBy);

    CashSessionDomain getActiveSession(Long outletId, Long employeeId);

    OutletCashSummaryDomain getOutletConsolidatedSummary(Long outletId);
}
