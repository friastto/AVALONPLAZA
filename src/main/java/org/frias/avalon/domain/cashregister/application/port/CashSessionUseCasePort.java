package org.frias.avalon.domain.cashregister.application.port;

import org.frias.avalon.domain.cashregister.application.dto.CashSessionResponse;
import org.frias.avalon.domain.cashregister.application.dto.CashierHistorySummaryResponse;
import org.frias.avalon.domain.cashregister.application.dto.ConsolidatedHistoryResponse;
import org.frias.avalon.domain.cashregister.application.dto.DiscrepancyHistoryResponse;
import org.frias.avalon.domain.cashregister.application.dto.OutletCashSummaryResponse;
import org.frias.avalon.domain.cashregister.application.dto.PageResponseDto;
import org.frias.avalon.domain.cashregister.application.dto.ThresholdConfigurationResponse;
import org.frias.avalon.domain.cashregister.domain.CashExpenseDomain;
import org.frias.avalon.domain.cashregister.domain.CashPickupDomain;
import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;
import org.frias.avalon.domain.cashregister.domain.OutletCashSummaryDomain;

import java.math.BigDecimal;
import java.util.List;

public interface CashSessionUseCasePort {

    CashSessionDomain openSession(Long outletId, Long employeeId, BigDecimal initialBase);

    CashSessionDomain closeSession(Long sessionId, BigDecimal actualCashContado, String notes);

    CashExpenseDomain registerExpense(Long sessionId, BigDecimal amount, String reason, Long registeredBy);

    CashSessionDomain getActiveSession(Long outletId, Long employeeId);

    OutletCashSummaryDomain getOutletConsolidatedSummary(Long outletId);

    void configureThreshold(Long outletId, BigDecimal thresholdAmount);

    ThresholdConfigurationResponse getThresholds(Long outletId);

    CashPickupDomain registerPickup(Long sessionId, BigDecimal amount, String reason, Long registeredBy);

    void submitBlindCountStep1(Long sessionId, Long employeeId, BigDecimal actualCash);

    void submitBlindCountStep2(Long sessionId, Long managerId, BigDecimal managerCountedCash, String justification);

    CashSessionDomain submitThreeStepAudit(Long sessionId, BigDecimal baseCash, BigDecimal remainingCash, String notes);

    CashSessionResponse getActiveSessionResponse(Long outletId, Long employeeId);

    OutletCashSummaryResponse getOutletConsolidatedSummaryResponse(Long outletId);

    List<CashierHistorySummaryResponse> getOutletCashiersHistory(Long outletId);

    PageResponseDto<ConsolidatedHistoryResponse> getConsolidatedHistory(Long outletId, Long employeeId, Integer year, Integer month, Integer day, int page, int size);

    PageResponseDto<DiscrepancyHistoryResponse> getDiscrepanciesHistory(Long outletId, Long employeeId, String discrepancyType, Integer year, Integer month, Integer day, int page, int size);
}
