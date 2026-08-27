package org.frias.avalon.domain.cashregister.application.usecase;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.cashregister.application.port.CashSessionRepositoryPort;
import org.frias.avalon.domain.cashregister.domain.CashExpenseDomain;
import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for CashSessionUseCaseImpl Application Layer")
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
    @DisplayName("Should open session successfully when no active session exists")
    void shouldOpenSessionSuccessfully() {
        when(cashSessionRepositoryPort.findActiveSession(1L, 10L)).thenReturn(Optional.empty());

        CashSessionDomain session = CashSessionDomain.open(1L, 10L, new BigDecimal("100.00"));
        when(cashSessionRepositoryPort.saveSession(any(CashSessionDomain.class))).thenReturn(session);

        CashSessionDomain result = cashSessionUseCase.openSession(1L, 10L, new BigDecimal("100.00"));

        assertNotNull(result);
        assertEquals("OPEN", result.getStatus());
        verify(cashSessionRepositoryPort, times(1)).saveSession(any(CashSessionDomain.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when opening session if active session already exists")
    void shouldThrowExceptionWhenActiveSessionExists() {
        CashSessionDomain existing = CashSessionDomain.open(1L, 10L, new BigDecimal("100.00"));
        when(cashSessionRepositoryPort.findActiveSession(1L, 10L)).thenReturn(Optional.of(existing));

        assertThrows(BusinessException.class, () -> cashSessionUseCase.openSession(1L, 10L, new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("Should close session successfully when status is BLIND_COUNTED")
    void shouldCloseSessionSuccessfully() {
        CashSessionDomain session = CashSessionDomain.fromPersistence(
                50L, 1L, 10L, LocalDateTime.now(), null, new BigDecimal("100.00"),
                new BigDecimal("250.00"), new BigDecimal("250.00"), BigDecimal.ZERO,
                "BLIND_COUNTED", "Notas", LocalDateTime.now(), LocalDateTime.now()
        );
        when(cashSessionRepositoryPort.findSessionById(50L)).thenReturn(Optional.of(session));
        when(cashSessionRepositoryPort.saveSession(any(CashSessionDomain.class))).thenReturn(session);

        CashSessionDomain result = cashSessionUseCase.closeSession(50L, new BigDecimal("250.00"), "Cierre correcto");

        assertNotNull(result);
        assertEquals("CLOSED", result.getStatus());
    }

    @Test
    @DisplayName("Should register expense in open cash session successfully")
    void shouldRegisterExpenseSuccessfully() {
        CashSessionDomain session = CashSessionDomain.open(1L, 10L, new BigDecimal("100.00"));
        when(cashSessionRepositoryPort.findSessionById(50L)).thenReturn(Optional.of(session));

        CashExpenseDomain expense = CashExpenseDomain.create(50L, new BigDecimal("30.00"), "Limpieza", 10L);
        when(cashSessionRepositoryPort.saveExpense(any(CashExpenseDomain.class))).thenReturn(expense);

        CashExpenseDomain result = cashSessionUseCase.registerExpense(50L, new BigDecimal("30.00"), "Limpieza", 10L);

        assertNotNull(result);
        assertEquals(new BigDecimal("30.00"), result.getAmount());
    }

    @Test
    @DisplayName("Should submit blind count step 1 and step 2 successfully")
    void shouldSubmitBlindCountStepsSuccessfully() {
        CashSessionDomain session = CashSessionDomain.open(1L, 10L, new BigDecimal("100.00"));
        when(cashSessionRepositoryPort.findSessionById(50L)).thenReturn(Optional.of(session));

        cashSessionUseCase.submitBlindCountStep1(50L, 10L, new BigDecimal("300.00"));
        verify(cashSessionRepositoryPort, times(1)).saveSession(session);

        cashSessionUseCase.submitBlindCountStep2(50L, 5L, new BigDecimal("300.00"), "Verificado");
        verify(cashSessionRepositoryPort, times(2)).saveSession(session);
    }
}
