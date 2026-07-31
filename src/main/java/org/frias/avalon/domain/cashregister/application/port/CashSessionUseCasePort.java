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

    void configureThreshold(Long outletId, BigDecimal thresholdAmount);

    org.frias.avalon.domain.cashregister.domain.CashPickupDomain registerPickup(Long sessionId, BigDecimal amount, String reason, Long registeredBy);

    void submitBlindCountStep1(Long sessionId, Long employeeId, BigDecimal actualCash);

    void submitBlindCountStep2(Long sessionId, Long managerId, BigDecimal managerCountedCash, String justification);

    CashSessionDomain submitThreeStepAudit(Long sessionId, BigDecimal baseCash, BigDecimal remainingCash, String notes);

    org.frias.avalon.domain.cashregister.presentation.dto.CashSessionResponse getActiveSessionResponse(Long outletId, Long employeeId);

    org.frias.avalon.domain.cashregister.presentation.dto.OutletCashSummaryResponse getOutletConsolidatedSummaryResponse(Long outletId);

    java.util.List<org.frias.avalon.domain.cashregister.presentation.dto.CashierHistorySummaryResponse> getOutletCashiersHistory(Long outletId);

    org.frias.avalon.domain.cashregister.presentation.dto.PageResponseDto<org.frias.avalon.domain.cashregister.presentation.dto.ConsolidatedHistoryResponse> getConsolidatedHistory(Long outletId, Long employeeId, Integer year, Integer month, Integer day, int page, int size);

    org.frias.avalon.domain.cashregister.presentation.dto.PageResponseDto<org.frias.avalon.domain.cashregister.presentation.dto.DiscrepancyHistoryResponse> getDiscrepanciesHistory(Long outletId, Long employeeId, String discrepancyType, Integer year, Integer month, Integer day, int page, int size);
}
