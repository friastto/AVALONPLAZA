package org.frias.avalon.domain.cashregister.application.usecase;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.cashregister.application.port.CashSessionRepositoryPort;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for CashSessionUseCaseImpl Application Service")
class CashSessionUseCaseImplTest {

    private CashSessionRepositoryPort cashSessionRepositoryPort;
    private SaleRepositoryPort saleRepositoryPort;
    private OutletRepositoryPort outletRepositoryPort;
    private PersonRepositoryPort personRepositoryPort;
    private UserAvalonRepositoryPort userRepositoryPort;
    private CompanyRepositoryPort companyRepositoryPort;
    private CashSessionUseCaseImpl cashSessionUseCase;

    @BeforeEach
    void setUp() {
        cashSessionRepositoryPort = mock(CashSessionRepositoryPort.class);
        saleRepositoryPort = mock(SaleRepositoryPort.class);
        outletRepositoryPort = mock(OutletRepositoryPort.class);
        personRepositoryPort = mock(PersonRepositoryPort.class);
        userRepositoryPort = mock(UserAvalonRepositoryPort.class);
        companyRepositoryPort = mock(CompanyRepositoryPort.class);

        cashSessionUseCase = new CashSessionUseCaseImpl(
                cashSessionRepositoryPort,
                saleRepositoryPort,
                outletRepositoryPort,
                personRepositoryPort,
                userRepositoryPort,
                companyRepositoryPort
        );
    }

    @Test
    @DisplayName("Should throw exception when opening session if an active session already exists")
    void shouldThrowExceptionWhenActiveSessionExists() {
        Long outletId = 1L;
        Long employeeId = 100L;
        BigDecimal initialBase = new BigDecimal("100.00");

        CashSessionDomain activeSession = CashSessionDomain.open(outletId, employeeId, initialBase);
        when(cashSessionRepositoryPort.findActiveSession(outletId, employeeId)).thenReturn(Optional.of(activeSession));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                cashSessionUseCase.openSession(outletId, employeeId, initialBase));

        assertTrue(exception.getMessage().contains("sesión de caja abierta"));
        verify(cashSessionRepositoryPort, never()).saveSession(any());
    }

    @Test
    @DisplayName("Should open cash session successfully when no active session exists")
    void shouldOpenCashSessionSuccessfully() {
        Long outletId = 1L;
        Long employeeId = 100L;
        BigDecimal initialBase = new BigDecimal("100.00");

        when(cashSessionRepositoryPort.findActiveSession(outletId, employeeId)).thenReturn(Optional.empty());
        when(cashSessionRepositoryPort.saveSession(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CashSessionDomain result = cashSessionUseCase.openSession(outletId, employeeId, initialBase);

        assertNotNull(result);
        assertEquals(outletId, result.getOutletId());
        assertEquals(employeeId, result.getEmployeeId());
        assertEquals(initialBase, result.getInitialBase());
        verify(cashSessionRepositoryPort, times(1)).saveSession(any());
    }
}
