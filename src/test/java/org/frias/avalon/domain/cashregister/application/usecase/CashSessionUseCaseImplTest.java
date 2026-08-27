package org.frias.avalon.domain.cashregister.application.usecase;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.cashregister.application.dto.CashSessionResponse;
import org.frias.avalon.domain.cashregister.application.dto.CashierHistorySummaryResponse;
import org.frias.avalon.domain.cashregister.application.dto.ConsolidatedHistoryResponse;
import org.frias.avalon.domain.cashregister.application.dto.DiscrepancyHistoryResponse;
import org.frias.avalon.domain.cashregister.application.dto.OutletCashSummaryResponse;
import org.frias.avalon.domain.cashregister.application.dto.PageResponseDto;
import org.frias.avalon.domain.cashregister.application.port.CashSessionRepositoryPort;
import org.frias.avalon.domain.cashregister.domain.CashExpenseDomain;
import org.frias.avalon.domain.cashregister.domain.CashPickupDomain;
import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;
import org.frias.avalon.domain.cashregister.domain.OutletCashSummaryDomain;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for CashSessionUseCaseImpl in Cash Register Domain")
class CashSessionUseCaseImplTest {

    private CashSessionRepositoryPort cashSessionRepositoryPort;
    private SaleRepositoryPort saleRepositoryPort;
    private OutletRepositoryPort outletRepositoryPort;
    private PersonRepositoryPort personRepositoryPort;
    private UserAvalonRepositoryPort userAvalonRepositoryPort;
    private CompanyRepositoryPort companyRepositoryPort;

    private CashSessionUseCaseImpl cashSessionUseCase;

    @BeforeEach
    void setUp() {
        cashSessionRepositoryPort = mock(CashSessionRepositoryPort.class);
        saleRepositoryPort = mock(SaleRepositoryPort.class);
        outletRepositoryPort = mock(OutletRepositoryPort.class);
        personRepositoryPort = mock(PersonRepositoryPort.class);
        userAvalonRepositoryPort = mock(UserAvalonRepositoryPort.class);
        companyRepositoryPort = mock(CompanyRepositoryPort.class);

        cashSessionUseCase = new CashSessionUseCaseImpl(
                cashSessionRepositoryPort,
                saleRepositoryPort,
                outletRepositoryPort,
                personRepositoryPort,
                userAvalonRepositoryPort,
                companyRepositoryPort
        );
    }

    @Test
    @DisplayName("Should open a new cash session successfully when no active session exists")
    void shouldOpenSessionSuccessfully() {
        when(cashSessionRepositoryPort.findActiveSession(1L, 10L)).thenReturn(Optional.empty());
        CashSessionDomain newSession = CashSessionDomain.open(1L, 10L, new BigDecimal("50000"));
        when(cashSessionRepositoryPort.saveSession(any(CashSessionDomain.class))).thenReturn(newSession);

        CashSessionDomain result = cashSessionUseCase.openSession(1L, 10L, new BigDecimal("50000"));

        assertNotNull(result);
        assertEquals(1L, result.getOutletId());
        assertEquals(10L, result.getEmployeeId());
        assertEquals(new BigDecimal("50000"), result.getInitialBase());
    }

    @Test
    @DisplayName("Should throw BusinessException when opening session if active session already exists")
    void shouldThrowExceptionWhenActiveSessionExists() {
        CashSessionDomain active = CashSessionDomain.open(1L, 10L, new BigDecimal("50000"));
        when(cashSessionRepositoryPort.findActiveSession(1L, 10L)).thenReturn(Optional.of(active));

        assertThrows(BusinessException.class, () -> cashSessionUseCase.openSession(1L, 10L, new BigDecimal("50000")));
    }

    @Test
    @DisplayName("Should close cash session successfully when blind counted")
    void shouldCloseSessionSuccessfully() {
        CashSessionDomain session = CashSessionDomain.fromPersistence(
                100L, 1L, 10L, LocalDateTime.now(), null,
                new BigDecimal("50000"), new BigDecimal("50000"), new BigDecimal("50000"), BigDecimal.ZERO,
                "BLIND_COUNTED", null, LocalDateTime.now(), LocalDateTime.now()
        );
        when(cashSessionRepositoryPort.findSessionById(100L)).thenReturn(Optional.of(session));
        when(cashSessionRepositoryPort.saveSession(any(CashSessionDomain.class))).thenAnswer(i -> i.getArgument(0));

        CashSessionDomain closed = cashSessionUseCase.closeSession(100L, new BigDecimal("50000"), "Cierre normal");

        assertNotNull(closed);
        assertEquals("CLOSED", closed.getStatus());
    }

    @Test
    @DisplayName("Should register cash expense successfully")
    void shouldRegisterExpenseSuccessfully() {
        CashSessionDomain session = CashSessionDomain.open(1L, 10L, new BigDecimal("50000"));
        when(cashSessionRepositoryPort.findSessionById(100L)).thenReturn(Optional.of(session));

        CashExpenseDomain expense = CashExpenseDomain.create(100L, new BigDecimal("10000"), "Insumos", 10L);
        when(cashSessionRepositoryPort.saveExpense(any(CashExpenseDomain.class))).thenReturn(expense);

        CashExpenseDomain result = cashSessionUseCase.registerExpense(100L, new BigDecimal("10000"), "Insumos", 10L);

        assertNotNull(result);
        assertEquals(new BigDecimal("10000"), result.getAmount());
    }

    @Test
    @DisplayName("Should register cash pickup successfully")
    void shouldRegisterPickupSuccessfully() {
        CashSessionDomain session = CashSessionDomain.open(1L, 10L, new BigDecimal("50000"));
        when(cashSessionRepositoryPort.findSessionById(100L)).thenReturn(Optional.of(session));

        CashPickupDomain pickup = CashPickupDomain.create(100L, 10L, new BigDecimal("20000"), "Excedente");
        when(cashSessionRepositoryPort.savePickup(any(CashPickupDomain.class))).thenReturn(pickup);

        CashPickupDomain result = cashSessionUseCase.registerPickup(100L, new BigDecimal("20000"), "Excedente", 10L);

        assertNotNull(result);
        assertEquals(new BigDecimal("20000"), result.getAmount());
    }

    @Test
    @DisplayName("Should submit blind count step 1 successfully")
    void shouldSubmitBlindCountStep1Successfully() {
        CashSessionDomain session = CashSessionDomain.open(1L, 10L, new BigDecimal("50000"));
        when(cashSessionRepositoryPort.findSessionById(100L)).thenReturn(Optional.of(session));
        when(userAvalonRepositoryPort.findById(10L)).thenReturn(Optional.empty());
        when(saleRepositoryPort.findByOutletAndEmployeeAndDateBetween(any(), any(), any(), any())).thenReturn(List.of());
        when(cashSessionRepositoryPort.findExpensesBySessionId(100L)).thenReturn(List.of());
        when(cashSessionRepositoryPort.findPickupsBySessionId(100L)).thenReturn(List.of());

        assertDoesNotThrow(() -> cashSessionUseCase.submitBlindCountStep1(100L, 10L, new BigDecimal("50000")));
        verify(cashSessionRepositoryPort).saveSession(any(CashSessionDomain.class));
    }

    @Test
    @DisplayName("Should submit blind count step 2 successfully")
    void shouldSubmitBlindCountStep2Successfully() {
        CashSessionDomain session = CashSessionDomain.fromPersistence(
                100L, 1L, 10L, LocalDateTime.now(), null,
                new BigDecimal("50000"), new BigDecimal("50000"), new BigDecimal("50000"), BigDecimal.ZERO,
                "BLIND_COUNTED", null, LocalDateTime.now(), LocalDateTime.now()
        );
        when(cashSessionRepositoryPort.findSessionById(100L)).thenReturn(Optional.of(session));

        assertDoesNotThrow(() -> cashSessionUseCase.submitBlindCountStep2(100L, 5L, new BigDecimal("50000"), "Correcto"));
        verify(cashSessionRepositoryPort).saveSession(any(CashSessionDomain.class));
    }

    @Test
    @DisplayName("Should submit three-step audit successfully")
    void shouldSubmitThreeStepAuditSuccessfully() {
        CashSessionDomain session = CashSessionDomain.open(1L, 10L, new BigDecimal("50000"));
        when(cashSessionRepositoryPort.findSessionById(100L)).thenReturn(Optional.of(session));
        when(userAvalonRepositoryPort.findById(10L)).thenReturn(Optional.empty());
        when(saleRepositoryPort.findByOutletAndEmployeeAndDateBetween(any(), any(), any(), any())).thenReturn(List.of());
        when(cashSessionRepositoryPort.findExpensesBySessionId(100L)).thenReturn(List.of());
        when(cashSessionRepositoryPort.findPickupsBySessionId(100L)).thenReturn(List.of());
        when(cashSessionRepositoryPort.saveSession(any())).thenAnswer(i -> i.getArgument(0));

        CashSessionDomain audited = cashSessionUseCase.submitThreeStepAudit(100L, new BigDecimal("20000"), new BigDecimal("30000"), "Audit ok");

        assertNotNull(audited);
        assertEquals("CLOSED", audited.getStatus());
    }

    @Test
    @DisplayName("Should map and enrich session response fully")
    void shouldMapAndEnrichSessionResponseFully() {
        CashSessionDomain session = CashSessionDomain.open(1L, 10L, new BigDecimal("50000"));
        
        UserAvalonDomain user = UserAvalonDomain.fromPersistenceAdvanced(10L, 20L, "cajero1", "salt", "hash", 1L);
        when(userAvalonRepositoryPort.findById(10L)).thenReturn(Optional.of(user));

        PersonDomain person = PersonDomain.createFromEntity(20L, "12345678", "CARLOS", "GOMEZ", "CALLE 1", 1L, 1L, 555L, "carlos@email.com", 1L, LocalDateTime.now(), LocalDateTime.now());
        when(personRepositoryPort.findById(20L)).thenReturn(Optional.of(person));

        SaleDomain sale1 = SaleDomain.fromPersistence(1L, UUID.randomUUID(), new BigDecimal("10000"), new BigDecimal("10000"), BigDecimal.ZERO, 1L, 1L, 20L, 1L, 20L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of());
        SaleDomain sale2 = SaleDomain.fromPersistence(2L, UUID.randomUUID(), new BigDecimal("20000"), new BigDecimal("20000"), BigDecimal.ZERO, 2L, 1L, 20L, 1L, 20L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of());
        SaleDomain sale3 = SaleDomain.fromPersistence(3L, UUID.randomUUID(), new BigDecimal("30000"), new BigDecimal("30000"), BigDecimal.ZERO, 3L, 1L, 20L, 1L, 20L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of());
        SaleDomain sale4 = SaleDomain.fromPersistence(4L, UUID.randomUUID(), new BigDecimal("40000"), new BigDecimal("40000"), BigDecimal.ZERO, 4L, 1L, 20L, 1L, 20L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of());

        when(saleRepositoryPort.findByOutletAndEmployeeAndDateBetween(eq(1L), eq(20L), any(), any()))
                .thenReturn(List.of(sale1, sale2, sale3, sale4));

        CashExpenseDomain exp = CashExpenseDomain.create(1L, new BigDecimal("5000"), "Servicios", 10L);
        when(cashSessionRepositoryPort.findExpensesBySessionId(any())).thenReturn(List.of(exp));

        CashPickupDomain pickup = CashPickupDomain.create(1L, 10L, new BigDecimal("15000"), "Retiro");
        when(cashSessionRepositoryPort.findPickupsBySessionId(any())).thenReturn(List.of(pickup));

        CashSessionResponse response = cashSessionUseCase.mapAndEnrichSessionResponse(session);

        assertNotNull(response);
        assertEquals(new BigDecimal("10000"), response.getSessionCashSales());
        assertEquals(new BigDecimal("20000"), response.getSessionCardSales());
        assertEquals(new BigDecimal("30000"), response.getSessionDigitalSales());
        assertEquals(new BigDecimal("40000"), response.getSessionCreditSales());
        assertEquals(new BigDecimal("5000"), response.getSessionExpenses());
        assertEquals(new BigDecimal("15000"), response.getTotalPickups());
        assertEquals("CARLOS GOMEZ", response.getEmployeeName());
    }

    @Test
    @DisplayName("Should get active session response for outlet and employee")
    void shouldGetActiveSessionResponse() {
        CashSessionDomain session = CashSessionDomain.open(1L, 10L, new BigDecimal("50000"));
        when(cashSessionRepositoryPort.findActiveSession(1L, 10L)).thenReturn(Optional.of(session));

        when(userAvalonRepositoryPort.findById(10L)).thenReturn(Optional.empty());
        when(saleRepositoryPort.findByOutletAndEmployeeAndDateBetween(any(), any(), any(), any())).thenReturn(List.of());
        when(cashSessionRepositoryPort.findExpensesBySessionId(any())).thenReturn(List.of());
        when(cashSessionRepositoryPort.findPickupsBySessionId(any())).thenReturn(List.of());

        CashSessionResponse response = cashSessionUseCase.getActiveSessionResponse(1L, 10L);

        assertNotNull(response);
        assertEquals(new BigDecimal("50000"), response.getInitialBase());
    }

    @Test
    @DisplayName("Should get outlet consolidated summary")
    void shouldGetOutletConsolidatedSummary() {
        CashSessionDomain active = CashSessionDomain.open(1L, 10L, new BigDecimal("50000"));
        when(cashSessionRepositoryPort.findActiveSessionsByOutlet(1L)).thenReturn(List.of(active));
        when(cashSessionRepositoryPort.findAllSessionsByOutlet(1L)).thenReturn(List.of(active));
        when(saleRepositoryPort.findByOutletAndDateBetween(eq(1L), any(), any())).thenReturn(List.of());
        when(cashSessionRepositoryPort.findExpensesBySessionIds(any())).thenReturn(List.of());
        when(cashSessionRepositoryPort.findPickupsBySessionId(any())).thenReturn(List.of());

        OutletDomain outlet = OutletDomain.create("Tienda Centro", "CALLE 1", "5551234", "123456", 1L, new LocationDomain(4.6, -74.0), new BigDecimal("1000000"));
        when(outletRepositoryPort.findById(1L)).thenReturn(Optional.of(outlet));

        OutletCashSummaryDomain summary = cashSessionUseCase.getOutletConsolidatedSummary(1L);

        assertNotNull(summary);
        assertEquals(1L, summary.getOutletId());
        assertEquals(1, summary.getActiveSessionsCount());
    }

    @Test
    @DisplayName("Should configure cash threshold for outlet")
    void shouldConfigureThreshold() {
        OutletDomain outlet = OutletDomain.create("Tienda Centro", "CALLE 1", "5551234", "123456", 1L, new LocationDomain(4.6, -74.0), new BigDecimal("1000000"));
        when(outletRepositoryPort.findById(1L)).thenReturn(Optional.of(outlet));

        assertDoesNotThrow(() -> cashSessionUseCase.configureThreshold(1L, new BigDecimal("2000000")));
        verify(outletRepositoryPort).update(any(OutletDomain.class));
    }

    @Test
    @DisplayName("Should get outlet cashiers history")
    void shouldGetOutletCashiersHistory() {
        when(cashSessionRepositoryPort.findDistinctEmployeeIdsByOutletId(1L)).thenReturn(List.of(10L));
        UserAvalonDomain user = UserAvalonDomain.fromPersistenceAdvanced(10L, 20L, "cajero1", "salt", "hash", 1L);
        when(userAvalonRepositoryPort.findById(10L)).thenReturn(Optional.of(user));

        PersonDomain person = PersonDomain.createFromEntity(20L, "12345678", "CARLOS", "GOMEZ", "CALLE 1", 1L, 1L, 555L, "carlos@email.com", 1L, LocalDateTime.now(), LocalDateTime.now());
        when(personRepositoryPort.findById(20L)).thenReturn(Optional.of(person));

        List<CashierHistorySummaryResponse> history = cashSessionUseCase.getOutletCashiersHistory(1L);

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals("CARLOS GOMEZ", history.get(0).fullName());
    }

    @Test
    @DisplayName("Should get consolidated history with pagination")
    void shouldGetConsolidatedHistory() {
        CashSessionDomain session = CashSessionDomain.open(1L, 10L, new BigDecimal("50000"));
        when(cashSessionRepositoryPort.findAllSessionsByOutlet(1L)).thenReturn(List.of(session));
        when(saleRepositoryPort.findByOutletAndDateBetween(eq(1L), any(), any())).thenReturn(List.of());

        PageResponseDto<ConsolidatedHistoryResponse> page = cashSessionUseCase.getConsolidatedHistory(1L, null, null, null, null, 0, 10);

        assertNotNull(page);
        assertEquals(1, page.content().size());
    }

    @Test
    @DisplayName("Should get discrepancies history with pagination")
    void shouldGetDiscrepanciesHistory() {
        CashSessionDomain session = CashSessionDomain.open(1L, 10L, new BigDecimal("50000"));
        when(cashSessionRepositoryPort.findDiscrepanciesHistory(eq(1L), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(session)));

        PageResponseDto<DiscrepancyHistoryResponse> page = cashSessionUseCase.getDiscrepanciesHistory(1L, null, null, null, null, null, 0, 10);

        assertNotNull(page);
        assertEquals(1, page.content().size());
    }
}
