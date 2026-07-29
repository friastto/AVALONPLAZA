package org.frias.avalon.domain.cashregister.application.usecase;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.cashregister.application.port.CashSessionRepositoryPort;
import org.frias.avalon.domain.cashregister.application.port.CashSessionUseCasePort;
import org.frias.avalon.domain.cashregister.domain.CashExpenseDomain;
import org.frias.avalon.domain.cashregister.domain.CashPickupDomain;
import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;
import org.frias.avalon.domain.cashregister.domain.OutletCashSummaryDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CashSessionUseCaseImpl implements CashSessionUseCasePort {

    private final CashSessionRepositoryPort cashSessionRepositoryPort;
    private final SaleRepositoryPort saleRepositoryPort;
    private final OutletRepositoryPort outletRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;

    @Override
    @Transactional
    public CashSessionDomain openSession(Long outletId, Long employeeId, BigDecimal initialBase) {
        Optional<CashSessionDomain> active = cashSessionRepositoryPort.findActiveSession(outletId, employeeId);
        if (active.isPresent()) {
            throw new BusinessException("El empleado ya tiene una sesión de caja abierta en esta tienda");
        }

        CashSessionDomain session = CashSessionDomain.open(outletId, employeeId, initialBase);
        return cashSessionRepositoryPort.saveSession(session);
    }

    @Override
    @Transactional
    public CashSessionDomain closeSession(Long sessionId, BigDecimal actualCashContado, String notes) {
        CashSessionDomain session = cashSessionRepositoryPort.findSessionById(sessionId)
                .orElseThrow(() -> new BusinessException("No se encontró la sesión de caja con id: " + sessionId));

        if (!"OPEN".equals(session.getStatus()) && !"BLIND_COUNTED".equals(session.getStatus()) && !"AUDITED".equals(session.getStatus())) {
            throw new BusinessException("La sesión de caja no puede cerrarse");
        }

        session.closeSession();
        return cashSessionRepositoryPort.saveSession(session);
    }

    @Override
    @Transactional
    public CashExpenseDomain registerExpense(Long sessionId, BigDecimal amount, String reason, Long registeredBy) {
        CashSessionDomain session = cashSessionRepositoryPort.findSessionById(sessionId)
                .orElseThrow(() -> new BusinessException("No se encontró la sesión de caja especificada"));

        if (!"OPEN".equals(session.getStatus())) {
            throw new BusinessException("No se pueden registrar egresos en una sesión de caja cerrada");
        }

        CashExpenseDomain expense = CashExpenseDomain.create(sessionId, amount, reason, registeredBy);
        return cashSessionRepositoryPort.saveExpense(expense);
    }

    @Override
    public CashSessionDomain getActiveSession(Long outletId, Long employeeId) {
        return cashSessionRepositoryPort.findActiveSession(outletId, employeeId)
                .orElseThrow(() -> new BusinessException("No hay ninguna sesión de caja abierta para este usuario en esta tienda"));
    }

    public org.frias.avalon.domain.cashregister.presentation.dto.CashSessionResponse mapAndEnrichSessionResponse(CashSessionDomain session) {
        if (session == null) return null;
        LocalDateTime now = LocalDateTime.now();
        List<SaleDomain> sessionSales = saleRepositoryPort.findByOutletAndEmployeeAndDateBetween(
                session.getOutletId(),
                session.getEmployeeId(),
                session.getOpenedAt(),
                now
        );

        BigDecimal cashSales = BigDecimal.ZERO;
        BigDecimal cardSales = BigDecimal.ZERO;
        BigDecimal digitalSales = BigDecimal.ZERO;
        BigDecimal creditSales = BigDecimal.ZERO;

        for (SaleDomain sale : sessionSales) {
            Long method = sale.getPaymentMethodId();
            BigDecimal amount = sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO;
            if (method != null) {
                if (method == 1L) cashSales = cashSales.add(amount);
                else if (method == 2L) cardSales = cardSales.add(amount);
                else if (method == 3L) digitalSales = digitalSales.add(amount);
                else if (method == 4L) creditSales = creditSales.add(amount);
                else cashSales = cashSales.add(amount);
            } else {
                cashSales = cashSales.add(amount);
            }
        }

        List<CashExpenseDomain> expenses = cashSessionRepositoryPort.findExpensesBySessionId(session.getId());
        BigDecimal sessionExpenses = BigDecimal.ZERO;
        for (CashExpenseDomain exp : expenses) {
            sessionExpenses = sessionExpenses.add(exp.getAmount());
        }

        List<CashPickupDomain> pickups = cashSessionRepositoryPort.findPickupsBySessionId(session.getId());
        BigDecimal sessionPickups = BigDecimal.ZERO;
        for (CashPickupDomain pickup : pickups) {
            sessionPickups = sessionPickups.add(pickup.getAmount());
        }

        BigDecimal expectedCash = session.getInitialBase().add(cashSales).subtract(sessionExpenses).subtract(sessionPickups);

        Optional<PersonDomain> personOpt = personRepositoryPort.findById(session.getEmployeeId());
        String name = personOpt.map(p -> (p.getName() + " " + (p.getLastName() != null ? p.getLastName() : "")).trim()).orElse("Empleado #" + session.getEmployeeId());
        String numberId = personOpt.map(PersonDomain::getNumberid).orElse(null);
        String regName = "Caja N° " + ((session.getId() == null || session.getId() % 2 != 0) ? "1" : "2");

        org.frias.avalon.domain.cashregister.presentation.dto.CashSessionResponse response = org.frias.avalon.domain.cashregister.presentation.dto.CashSessionResponse.fromDomain(session);
        response.setSessionCashSales(cashSales);
        response.setSessionCardSales(cardSales);
        response.setSessionDigitalSales(digitalSales);
        response.setSessionCreditSales(creditSales);
        response.setSessionExpenses(sessionExpenses);
        response.setTotalPickups(sessionPickups);
        response.setExpectedCash(expectedCash);
        response.setEmployeeName(name);
        response.setEmployeeNumberId(numberId);
        response.setRegisterName(regName);
        return response;
    }

    @Override
    public org.frias.avalon.domain.cashregister.presentation.dto.CashSessionResponse getActiveSessionResponse(Long outletId, Long employeeId) {
        CashSessionDomain session = getActiveSession(outletId, employeeId);
        return mapAndEnrichSessionResponse(session);
    }

    @Override
    public OutletCashSummaryDomain getOutletConsolidatedSummary(Long outletId) {
        List<CashSessionDomain> activeSessions = cashSessionRepositoryPort.findActiveSessionsByOutlet(outletId);
        List<CashSessionDomain> allSessions = cashSessionRepositoryPort.findAllSessionsByOutlet(outletId);

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime now = LocalDateTime.now();

        List<SaleDomain> todaySales = saleRepositoryPort.findByOutletAndDateBetween(outletId, startOfDay, now);

        BigDecimal totalCash = BigDecimal.ZERO;
        BigDecimal totalDigital = BigDecimal.ZERO;
        BigDecimal totalCard = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (SaleDomain sale : todaySales) {
            Long method = sale.getPaymentMethodId();
            BigDecimal amount = sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO;

            if (method != null) {
                if (method == 1L) totalCash = totalCash.add(amount);
                else if (method == 2L) totalCard = totalCard.add(amount);
                else if (method == 3L) totalDigital = totalDigital.add(amount);
                else if (method == 4L) totalCredit = totalCredit.add(amount);
                else totalCash = totalCash.add(amount);
            } else {
                totalCash = totalCash.add(amount);
            }
        }

        List<Long> activeSessionIds = activeSessions.stream().map(CashSessionDomain::getId).toList();
        List<CashExpenseDomain> todayExpenses = cashSessionRepositoryPort.findExpensesBySessionIds(activeSessionIds);
        BigDecimal totalExpenses = BigDecimal.ZERO;
        for (CashExpenseDomain exp : todayExpenses) {
            totalExpenses = totalExpenses.add(exp.getAmount());
        }

        BigDecimal totalPickups = BigDecimal.ZERO;
        for (Long sessionId : activeSessionIds) {
            List<CashPickupDomain> pickups = cashSessionRepositoryPort.findPickupsBySessionId(sessionId);
            for (CashPickupDomain pickup : pickups) {
                totalPickups = totalPickups.add(pickup.getAmount());
            }
        }

        BigDecimal activeBases = BigDecimal.ZERO;
        for (CashSessionDomain session : activeSessions) {
            activeBases = activeBases.add(session.getInitialBase());
        }

        BigDecimal currentExpectedCashInStore = activeBases.add(totalCash).subtract(totalExpenses).subtract(totalPickups);

        int closedCount = (int) allSessions.stream().filter(s -> "CLOSED".equals(s.getStatus())).count();

        OutletDomain outlet = outletRepositoryPort.findById(outletId).orElse(null);
        BigDecimal cashThresholdAmount = outlet != null ? outlet.getCashThresholdAmount() : null;
        
        Boolean thresholdExceeded = false;
        if (cashThresholdAmount != null && cashThresholdAmount.compareTo(BigDecimal.ZERO) > 0) {
            thresholdExceeded = currentExpectedCashInStore.compareTo(cashThresholdAmount) >= 0;
        }

        return new OutletCashSummaryDomain(
                outletId,
                totalCash,
                totalDigital,
                totalCard,
                totalCredit,
                totalExpenses,
                currentExpectedCashInStore,
                activeSessions.size(),
                closedCount,
                activeSessions,
                totalPickups,
                cashThresholdAmount,
                thresholdExceeded
        );
    }

    @Override
    public org.frias.avalon.domain.cashregister.presentation.dto.OutletCashSummaryResponse getOutletConsolidatedSummaryResponse(Long outletId) {
        OutletCashSummaryDomain domain = getOutletConsolidatedSummary(outletId);
        List<org.frias.avalon.domain.cashregister.presentation.dto.CashSessionResponse> enrichedSessions = domain.getActiveSessions() != null
                ? domain.getActiveSessions().stream().map(this::mapAndEnrichSessionResponse).toList()
                : List.of();

        return org.frias.avalon.domain.cashregister.presentation.dto.OutletCashSummaryResponse.builder()
                .outletId(domain.getOutletId())
                .totalCashSales(domain.getTotalCashSales())
                .totalDigitalSales(domain.getTotalDigitalSales())
                .totalCardSales(domain.getTotalCardSales())
                .totalCreditSales(domain.getTotalCreditSales())
                .totalExpenses(domain.getTotalExpenses())
                .currentExpectedCashInStore(domain.getCurrentExpectedCashInStore())
                .activeSessionsCount(domain.getActiveSessionsCount())
                .closedSessionsCount(domain.getClosedSessionsCount())
                .activeSessions(enrichedSessions)
                .totalPickups(domain.getTotalPickups())
                .cashThresholdAmount(domain.getCashThresholdAmount())
                .thresholdExceeded(domain.getThresholdExceeded())
                .build();
    }

    @Override
    @Transactional
    public void configureThreshold(Long outletId, BigDecimal thresholdAmount) {
        OutletDomain outlet = outletRepositoryPort.findById(outletId)
                .orElseThrow(() -> new BusinessException("No se encontró la tienda"));
        outlet.setCashThresholdAmount(thresholdAmount);
        outletRepositoryPort.update(outlet);
    }

    @Override
    @Transactional
    public CashPickupDomain registerPickup(Long sessionId, BigDecimal amount, String reason, Long registeredBy) {
        CashSessionDomain session = cashSessionRepositoryPort.findSessionById(sessionId)
                .orElseThrow(() -> new BusinessException("No se encontró la sesión de caja especificada"));

        if (!"OPEN".equals(session.getStatus())) {
            throw new BusinessException("No se pueden registrar retiros parciales en una sesión de caja cerrada");
        }

        CashPickupDomain pickup = CashPickupDomain.create(sessionId, registeredBy, amount, reason);
        return cashSessionRepositoryPort.savePickup(pickup);
    }

    @Override
    @Transactional
    public void submitBlindCountStep1(Long sessionId, Long employeeId, BigDecimal actualCash) {
        CashSessionDomain session = cashSessionRepositoryPort.findSessionById(sessionId)
                .orElseThrow(() -> new BusinessException("No se encontró la sesión de caja especificada"));
                
        LocalDateTime endDate = LocalDateTime.now();
        List<SaleDomain> sales = saleRepositoryPort.findByOutletAndEmployeeAndDateBetween(
                session.getOutletId(),
                session.getEmployeeId(),
                session.getOpenedAt(),
                endDate
        );

        BigDecimal totalSalesCash = BigDecimal.ZERO;
        for (SaleDomain sale : sales) {
            if (sale.getPaymentMethodId() != null && sale.getPaymentMethodId() == 1L) {
                totalSalesCash = totalSalesCash.add(sale.getTotalAmount());
            }
        }

        List<CashExpenseDomain> expenses = cashSessionRepositoryPort.findExpensesBySessionId(sessionId);
        BigDecimal totalExpensesCash = BigDecimal.ZERO;
        for (CashExpenseDomain expense : expenses) {
            totalExpensesCash = totalExpensesCash.add(expense.getAmount());
        }

        List<CashPickupDomain> pickups = cashSessionRepositoryPort.findPickupsBySessionId(sessionId);
        BigDecimal totalPickups = BigDecimal.ZERO;
        for (CashPickupDomain pickup : pickups) {
            totalPickups = totalPickups.add(pickup.getAmount());
        }

        session.blindCount(actualCash, totalSalesCash, totalExpensesCash, totalPickups, null);
        cashSessionRepositoryPort.saveSession(session);
    }

    @Override
    @Transactional
    public void submitBlindCountStep2(Long sessionId, Long managerId, BigDecimal managerCountedCash, String justification) {
        CashSessionDomain session = cashSessionRepositoryPort.findSessionById(sessionId)
                .orElseThrow(() -> new BusinessException("No se encontró la sesión de caja especificada"));

        session.audit();
        session.closeSession();
        cashSessionRepositoryPort.saveSession(session);
    }

    @Override
    @Transactional
    public CashSessionDomain submitThreeStepAudit(Long sessionId, BigDecimal baseCash, BigDecimal remainingCash, String notes) {
        CashSessionDomain session = cashSessionRepositoryPort.findSessionById(sessionId)
                .orElseThrow(() -> new BusinessException("No se encontró la sesión de caja especificada"));

        BigDecimal base = baseCash != null ? baseCash : BigDecimal.ZERO;
        BigDecimal remaining = remainingCash != null ? remainingCash : BigDecimal.ZERO;
        BigDecimal totalActual = base.add(remaining);

        LocalDateTime endDate = LocalDateTime.now();
        List<SaleDomain> sales = saleRepositoryPort.findByOutletAndEmployeeAndDateBetween(
                session.getOutletId(),
                session.getEmployeeId(),
                session.getOpenedAt(),
                endDate
        );

        BigDecimal totalSalesCash = BigDecimal.ZERO;
        for (SaleDomain sale : sales) {
            if (sale.getPaymentMethodId() != null && sale.getPaymentMethodId() == 1L) {
                totalSalesCash = totalSalesCash.add(sale.getTotalAmount());
            }
        }

        List<CashExpenseDomain> expenses = cashSessionRepositoryPort.findExpensesBySessionId(sessionId);
        BigDecimal totalExpensesCash = BigDecimal.ZERO;
        for (CashExpenseDomain expense : expenses) {
            totalExpensesCash = totalExpensesCash.add(expense.getAmount());
        }

        List<CashPickupDomain> pickups = cashSessionRepositoryPort.findPickupsBySessionId(sessionId);
        BigDecimal totalPickups = BigDecimal.ZERO;
        for (CashPickupDomain pickup : pickups) {
            totalPickups = totalPickups.add(pickup.getAmount());
        }

        session.blindCount(totalActual, totalSalesCash, totalExpensesCash, totalPickups, notes);
        session.closeSession();
        return cashSessionRepositoryPort.saveSession(session);
    }
}
