package org.frias.avalon.domain.cashregister.application.usecase;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.cashregister.application.port.CashSessionRepositoryPort;
import org.frias.avalon.domain.cashregister.application.port.CashSessionUseCasePort;
import org.frias.avalon.domain.cashregister.domain.CashExpenseDomain;
import org.frias.avalon.domain.cashregister.domain.CashPickupDomain;
import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;
import org.frias.avalon.domain.cashregister.domain.OutletCashSummaryDomain;
import org.frias.avalon.domain.cashregister.application.dto.CashSessionResponse;
import org.frias.avalon.domain.cashregister.application.dto.CashierHistorySummaryResponse;
import org.frias.avalon.domain.cashregister.application.dto.ConsolidatedHistoryResponse;
import org.frias.avalon.domain.cashregister.application.dto.DiscrepancyHistoryResponse;
import org.frias.avalon.domain.cashregister.application.dto.OutletCashSummaryResponse;
import org.frias.avalon.domain.cashregister.application.dto.PageResponseDto;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CashSessionUseCaseImpl implements CashSessionUseCasePort {

    private final CashSessionRepositoryPort cashSessionRepositoryPort;
    private final SaleRepositoryPort saleRepositoryPort;
    private final OutletRepositoryPort outletRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final CompanyRepositoryPort companyRepositoryPort;

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

    public CashSessionResponse mapAndEnrichSessionResponse(CashSessionDomain session) {
        if (session == null) return null;
        LocalDateTime now = LocalDateTime.now();
        Long personIdToQuery = session.getEmployeeId();
        if (session.getEmployeeId() != null) {
            Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findById(session.getEmployeeId());
            if (userOpt.isPresent() && userOpt.get().getPersonId() != null) {
                personIdToQuery = userOpt.get().getPersonId();
            }
        }

        List<SaleDomain> sessionSales = saleRepositoryPort.findByOutletAndEmployeeAndDateBetween(
                session.getOutletId(),
                personIdToQuery,
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

        String name = "Empleado #" + session.getEmployeeId();
        String numberId = null;

        if (session.getEmployeeId() != null) {
            Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findById(session.getEmployeeId());
            if (userOpt.isPresent()) {
                UserAvalonDomain user = userOpt.get();
                if (user.getPersonId() != null) {
                    Optional<PersonDomain> personOpt = personRepositoryPort.findById(user.getPersonId());
                    if (personOpt.isPresent()) {
                        PersonDomain person = personOpt.get();
                        name = (person.getName() + " " + (person.getLastName() != null ? person.getLastName() : "")).trim();
                        numberId = person.getNumberid();
                    }
                }
            } else {
                Optional<PersonDomain> personOpt = personRepositoryPort.findById(session.getEmployeeId());
                if (personOpt.isPresent()) {
                    PersonDomain person = personOpt.get();
                    name = (person.getName() + " " + (person.getLastName() != null ? person.getLastName() : "")).trim();
                    numberId = person.getNumberid();
                }
            }
        }
        String regName = "Caja N° " + ((session.getId() == null || session.getId() % 2 != 0) ? "1" : "2");

        CashSessionResponse response = CashSessionResponse.fromDomain(session);
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
    public CashSessionResponse getActiveSessionResponse(Long outletId, Long employeeId) {
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
        if (cashThresholdAmount == null && outlet != null && outlet.getCompanyId() != null) {
            cashThresholdAmount = companyRepositoryPort.findById(outlet.getCompanyId())
                    .map(CompanyDomain::defaultCashThresholdAmount)
                    .orElse(null);
        }
        
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
    public OutletCashSummaryResponse getOutletConsolidatedSummaryResponse(Long outletId) {
        OutletCashSummaryDomain domain = getOutletConsolidatedSummary(outletId);
        List<CashSessionResponse> enrichedSessions = domain.getActiveSessions() != null
                ? domain.getActiveSessions().stream().map(this::mapAndEnrichSessionResponse).toList()
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
        Long personIdToQuery = session.getEmployeeId();
        if (session.getEmployeeId() != null) {
            Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findById(session.getEmployeeId());
            if (userOpt.isPresent() && userOpt.get().getPersonId() != null) {
                personIdToQuery = userOpt.get().getPersonId();
            }
        }

        List<SaleDomain> sales = saleRepositoryPort.findByOutletAndEmployeeAndDateBetween(
                session.getOutletId(),
                personIdToQuery,
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
        Long personIdToQuery = session.getEmployeeId();
        if (session.getEmployeeId() != null) {
            Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findById(session.getEmployeeId());
            if (userOpt.isPresent() && userOpt.get().getPersonId() != null) {
                personIdToQuery = userOpt.get().getPersonId();
            }
        }

        List<SaleDomain> sales = saleRepositoryPort.findByOutletAndEmployeeAndDateBetween(
                session.getOutletId(),
                personIdToQuery,
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

    @Override
    public List<CashierHistorySummaryResponse> getOutletCashiersHistory(Long outletId) {
        List<Long> userIds = cashSessionRepositoryPort.findDistinctEmployeeIdsByOutletId(outletId);
        List<CashierHistorySummaryResponse> result = new ArrayList<>();
        for (Long userId : userIds) {
            Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findById(userId);
            String fullName = "Empleado #" + userId;
            String numberId = "N/A";
            Long statusId = 1L;

            if (userOpt.isPresent()) {
                UserAvalonDomain user = userOpt.get();
                statusId = user.getStatusId() != null ? user.getStatusId() : 1L;

                if (user.getPersonId() != null) {
                    Optional<PersonDomain> personOpt = personRepositoryPort.findById(user.getPersonId());
                    if (personOpt.isPresent()) {
                        PersonDomain person = personOpt.get();
                        fullName = (person.getName() + " " + (person.getLastName() != null ? person.getLastName() : "")).trim();
                        numberId = person.getNumberid() != null ? person.getNumberid() : "N/A";
                    }
                }
            } else {
                Optional<PersonDomain> personOpt = personRepositoryPort.findById(userId);
                if (personOpt.isPresent()) {
                    PersonDomain person = personOpt.get();
                    fullName = (person.getName() + " " + (person.getLastName() != null ? person.getLastName() : "")).trim();
                    numberId = person.getNumberid() != null ? person.getNumberid() : "N/A";
                }
            }

            result.add(new CashierHistorySummaryResponse(
                    userId,
                    userId,
                    fullName,
                    numberId,
                    statusId,
                    0L
            ));
        }
        return result;
    }

    @Override
    public PageResponseDto<ConsolidatedHistoryResponse> getConsolidatedHistory(Long outletId, Long employeeId, Integer year, Integer month, Integer day, int page, int size) {
        List<CashSessionDomain> allSessions = cashSessionRepositoryPort.findAllSessionsByOutlet(outletId);
        
        Map<String, List<CashSessionDomain>> groupedByDate = new TreeMap<>(Collections.reverseOrder());
        for (CashSessionDomain session : allSessions) {
            if (employeeId != null && !employeeId.equals(session.getEmployeeId())) {
                continue;
            }
            LocalDateTime dateToUse = session.getClosedAt() != null ? session.getClosedAt() : session.getOpenedAt();
            if (dateToUse == null) continue;

            if (year != null && dateToUse.getYear() != year) continue;
            if (month != null && dateToUse.getMonthValue() != month) continue;
            if (day != null && dateToUse.getDayOfMonth() != day) continue;

            String dateKey = dateToUse.toLocalDate().toString();
            groupedByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(session);
        }

        List<String> dates = new ArrayList<>(groupedByDate.keySet());
        int totalElements = dates.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<String> pagedDates = dates.subList(fromIndex, toIndex);
        List<ConsolidatedHistoryResponse> content = new ArrayList<>();

        for (String dateStr : pagedDates) {
            LocalDate localDate = LocalDate.parse(dateStr);
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = localDate.atTime(23, 59, 59);

            List<SaleDomain> sales = employeeId != null
                    ? saleRepositoryPort.findByOutletAndEmployeeAndDateBetween(outletId, employeeId, startOfDay, endOfDay)
                    : saleRepositoryPort.findByOutletAndDateBetween(outletId, startOfDay, endOfDay);

            BigDecimal totalCash = BigDecimal.ZERO;
            BigDecimal totalDigital = BigDecimal.ZERO;
            BigDecimal totalCard = BigDecimal.ZERO;
            BigDecimal totalCredit = BigDecimal.ZERO;

            for (SaleDomain sale : sales) {
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

            BigDecimal totalConsolidated = totalCash.add(totalDigital).add(totalCard).add(totalCredit);

            List<CashSessionDomain> daySessions = groupedByDate.get(dateStr);
            int activeCount = (int) daySessions.stream().filter(s -> "OPEN".equals(s.getStatus())).count();
            int closedCount = (int) daySessions.stream().filter(s -> !"OPEN".equals(s.getStatus())).count();

            content.add(new ConsolidatedHistoryResponse(
                    dateStr,
                    outletId,
                    totalConsolidated,
                    totalCash,
                    totalDigital,
                    totalCard,
                    totalCredit,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    activeCount,
                    closedCount
            ));
        }

        boolean isLast = (page + 1) >= totalPages || totalPages == 0;
        return new PageResponseDto<>(content, page, size, totalElements, totalPages, isLast);
    }

    @Override
    public PageResponseDto<DiscrepancyHistoryResponse> getDiscrepanciesHistory(Long outletId, Long employeeId, String discrepancyType, Integer year, Integer month, Integer day, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CashSessionDomain> domainPage = cashSessionRepositoryPort.findDiscrepanciesHistory(outletId, employeeId, discrepancyType, year, month, day, pageable);

        List<DiscrepancyHistoryResponse> content = domainPage.getContent().stream().map(session -> {
            String name = "Empleado #" + session.getEmployeeId();
            String numberId = "N/A";
            Long statusId = 1L;

            if (session.getEmployeeId() != null) {
                Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findById(session.getEmployeeId());
                if (userOpt.isPresent()) {
                    UserAvalonDomain user = userOpt.get();
                    statusId = user.getStatusId() != null ? user.getStatusId() : 1L;
                    if (user.getPersonId() != null) {
                        Optional<PersonDomain> personOpt = personRepositoryPort.findById(user.getPersonId());
                        if (personOpt.isPresent()) {
                            PersonDomain person = personOpt.get();
                            name = (person.getName() + " " + (person.getLastName() != null ? person.getLastName() : "")).trim();
                            numberId = person.getNumberid() != null ? person.getNumberid() : "N/A";
                        }
                    }
                } else {
                    Optional<PersonDomain> personOpt = personRepositoryPort.findById(session.getEmployeeId());
                    if (personOpt.isPresent()) {
                        PersonDomain person = personOpt.get();
                        name = (person.getName() + " " + (person.getLastName() != null ? person.getLastName() : "")).trim();
                        numberId = person.getNumberid() != null ? person.getNumberid() : "N/A";
                    }
                }
            }

            String typeStr = session.getDifference() != null && session.getDifference().compareTo(BigDecimal.ZERO) < 0 ? "SHORTAGE" : "SURPLUS";

            return new DiscrepancyHistoryResponse(
                    session.getId(),
                    session.getOutletId(),
                    session.getEmployeeId(),
                    name,
                    numberId,
                    statusId,
                    session.getInitialBase(),
                    session.getExpectedCash(),
                    session.getActualCash(),
                    session.getDifference(),
                    typeStr,
                    session.getNotes(),
                    session.getStatus(),
                    session.getOpenedAt(),
                    session.getClosedAt()
            );
        }).toList();

        return new PageResponseDto<>(
                content,
                domainPage.getNumber(),
                domainPage.getSize(),
                domainPage.getTotalElements(),
                domainPage.getTotalPages(),
                domainPage.isLast()
        );
    }
}
