package org.frias.avalon.domain.cashregister.application.port;

import org.frias.avalon.domain.cashregister.domain.CashExpenseDomain;
import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;

import java.util.List;
import java.util.Optional;

public interface CashSessionRepositoryPort {

    CashSessionDomain saveSession(CashSessionDomain session);

    Optional<CashSessionDomain> findSessionById(Long id);

    Optional<CashSessionDomain> findActiveSession(Long outletId, Long employeeId);

    List<CashSessionDomain> findActiveSessionsByOutlet(Long outletId);

    List<CashSessionDomain> findAllSessionsByOutlet(Long outletId);

    CashExpenseDomain saveExpense(CashExpenseDomain expense);

    List<CashExpenseDomain> findExpensesBySessionId(Long cashSessionId);

    List<CashExpenseDomain> findExpensesBySessionIds(List<Long> cashSessionIds);

    org.frias.avalon.domain.cashregister.domain.CashPickupDomain savePickup(org.frias.avalon.domain.cashregister.domain.CashPickupDomain pickup);

    List<org.frias.avalon.domain.cashregister.domain.CashPickupDomain> findPickupsBySessionId(Long cashSessionId);

    List<Long> findDistinctEmployeeIdsByOutletId(Long outletId);

    org.springframework.data.domain.Page<CashSessionDomain> findDiscrepanciesHistory(Long outletId, Long employeeId, String discrepancyType, Integer year, Integer month, Integer day, org.springframework.data.domain.Pageable pageable);
}
