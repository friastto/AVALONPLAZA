package org.frias.avalon.domain.cashregister.application.usecase;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.cashregister.application.port.CashSessionRepositoryPort;
import org.frias.avalon.domain.cashregister.application.port.CashSessionUseCasePort;
import org.frias.avalon.domain.cashregister.domain.CashExpenseDomain;
import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;
import org.frias.avalon.domain.cashregister.domain.OutletCashSummaryDomain;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CashSessionUseCaseImpl implements CashSessionUseCasePort {

    private final CashSessionRepositoryPort cashSessionRepositoryPort;
    private final SaleRepositoryPort saleRepositoryPort;

    @Override
    public CashSessionDomain openSession(Long outletId, Long employeeId, BigDecimal initialBase) {
        Optional<CashSessionDomain> active = cashSessionRepositoryPort.findActiveSession(outletId, employeeId);
        if (active.isPresent()) {
            throw new BusinessException("El empleado ya tiene una sesión de caja abierta en esta tienda");
        }

        CashSessionDomain session = CashSessionDomain.open(outletId, employeeId, initialBase);
        return cashSessionRepositoryPort.saveSession(session);
    }

    @Override
    public CashSessionDomain closeSession(Long sessionId, BigDecimal actualCashContado, String notes) {
        CashSessionDomain session = cashSessionRepositoryPort.findSessionById(sessionId)
                .orElseThrow(() -> new BusinessException("No se encontró la sesión de caja con id: " + sessionId));

        if (!"OPEN".equals(session.getStatus())) {
            throw new BusinessException("La sesión de caja ya se encuentra cerrada");
        }

        LocalDateTime endDate = LocalDateTime.now();
        List<SaleDomain> sales = saleRepositoryPort.findByOutletAndEmployeeAndDateBetween(
                session.getOutletId(),
                session.getEmployeeId(),
                session.getOpenedAt(),
                endDate
        );

        BigDecimal totalSalesCash = BigDecimal.ZERO;
        for (SaleDomain sale : sales) {
            // Payment Method 1 = Efectivo
            if (sale.getPaymentMethodId() != null && sale.getPaymentMethodId() == 1L) {
                totalSalesCash = totalSalesCash.add(sale.getTotalAmount());
            }
        }

        List<CashExpenseDomain> expenses = cashSessionRepositoryPort.findExpensesBySessionId(sessionId);
        BigDecimal totalExpensesCash = BigDecimal.ZERO;
        for (CashExpenseDomain expense : expenses) {
            totalExpensesCash = totalExpensesCash.add(expense.getAmount());
        }

        session.close(actualCashContado, totalSalesCash, totalExpensesCash, notes);
        return cashSessionRepositoryPort.saveSession(session);
    }

    @Override
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

        BigDecimal activeBases = BigDecimal.ZERO;
        for (CashSessionDomain session : activeSessions) {
            activeBases = activeBases.add(session.getInitialBase());
        }

        BigDecimal currentExpectedCashInStore = activeBases.add(totalCash).subtract(totalExpenses);

        int closedCount = (int) allSessions.stream().filter(s -> "CLOSED".equals(s.getStatus())).count();

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
                activeSessions
        );
    }
}
