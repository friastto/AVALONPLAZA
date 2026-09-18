package org.frias.avalon.domain.cashregister.application.port;

import org.frias.avalon.domain.cashregister.domain.CashExpenseDomain;
import org.frias.avalon.domain.cashregister.domain.CashPickupDomain;
import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    CashPickupDomain savePickup(CashPickupDomain pickup);

    List<CashPickupDomain> findPickupsBySessionId(Long cashSessionId);

    List<Long> findDistinctEmployeeIdsByOutletId(Long outletId);

    Page<CashSessionDomain> findDiscrepanciesHistory(Long outletId, Long employeeId, String discrepancyType, Integer year, Integer month, Integer day, Pageable pageable);
}
