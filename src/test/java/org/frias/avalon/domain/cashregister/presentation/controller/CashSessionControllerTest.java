package org.frias.avalon.domain.cashregister.presentation.controller;

import org.frias.avalon.domain.cashregister.application.dto.*;
import org.frias.avalon.domain.cashregister.application.port.CashSessionUseCasePort;
import org.frias.avalon.domain.cashregister.domain.CashExpenseDomain;
import org.frias.avalon.domain.cashregister.domain.CashPickupDomain;
import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for CashSessionController REST Presentation Layer")
class CashSessionControllerTest {

    private CashSessionUseCasePort cashSessionUseCasePort;
    private CashSessionController cashSessionController;

    @BeforeEach
    void setUp() {
        cashSessionUseCasePort = mock(CashSessionUseCasePort.class);
        cashSessionController = new CashSessionController(cashSessionUseCasePort);
    }

    @Test
    @DisplayName("Should open session and return HTTP 201 Created")
    void shouldOpenSessionSuccessfully() {
        OpenCashSessionRequest request = new OpenCashSessionRequest();
        request.setOutletId(1L);
        request.setEmployeeId(10L);
        request.setInitialBase(new BigDecimal("100.00"));

        CashSessionDomain session = CashSessionDomain.open(1L, 10L, new BigDecimal("100.00"));

        when(cashSessionUseCasePort.openSession(1L, 10L, new BigDecimal("100.00"))).thenReturn(session);

        ResponseEntity<CashSessionResponse> response = cashSessionController.openSession(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OPEN", response.getBody().getStatus());
    }

    @Test
    @DisplayName("Should close session and return HTTP 200 OK")
    void shouldCloseSessionSuccessfully() {
        CloseCashSessionRequest request = new CloseCashSessionRequest();
        request.setActualCash(new BigDecimal("250.00"));
        request.setNotes("Cierre de turno normal");

        CashSessionDomain session = CashSessionDomain.open(1L, 10L, new BigDecimal("100.00"));

        when(cashSessionUseCasePort.closeSession(50L, new BigDecimal("250.00"), "Cierre de turno normal")).thenReturn(session);

        ResponseEntity<CashSessionResponse> response = cashSessionController.closeSession(50L, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should submit three-step audit and return HTTP 200 OK")
    void shouldSubmitThreeStepAuditSuccessfully() {
        ThreeStepAuditRequest request = new ThreeStepAuditRequest();
        request.setBaseCash(new BigDecimal("100.00"));
        request.setRemainingCash(new BigDecimal("150.00"));
        request.setNotes("Auditoria 3 pasos");

        CashSessionDomain session = CashSessionDomain.open(1L, 10L, new BigDecimal("100.00"));

        when(cashSessionUseCasePort.submitThreeStepAudit(50L, new BigDecimal("100.00"), new BigDecimal("150.00"), "Auditoria 3 pasos"))
                .thenReturn(session);

        ResponseEntity<CashSessionResponse> response = cashSessionController.submitThreeStepAudit(50L, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should register expense and return HTTP 201 Created")
    void shouldRegisterExpenseSuccessfully() {
        CashExpenseRequest request = new CashExpenseRequest();
        request.setAmount(new BigDecimal("35.00"));
        request.setReason("Compra insumos de limpieza");
        request.setRegisteredBy(10L);

        CashExpenseDomain expense = CashExpenseDomain.create(50L, new BigDecimal("35.00"), "Compra insumos de limpieza", 10L);

        when(cashSessionUseCasePort.registerExpense(50L, new BigDecimal("35.00"), "Compra insumos de limpieza", 10L))
                .thenReturn(expense);

        ResponseEntity<CashExpenseDomain> response = cashSessionController.registerExpense(50L, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(new BigDecimal("35.00"), response.getBody().getAmount());
    }

    @Test
    @DisplayName("Should get active session response and return HTTP 200 OK")
    void shouldGetActiveSessionSuccessfully() {
        CashSessionResponse expected = CashSessionResponse.builder()
                .id(50L)
                .outletId(1L)
                .employeeId(10L)
                .status("OPEN")
                .build();

        when(cashSessionUseCasePort.getActiveSessionResponse(1L, 10L)).thenReturn(expected);

        ResponseEntity<CashSessionResponse> response = cashSessionController.getActiveSession(1L, 10L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(50L, response.getBody().getId());
    }

    @Test
    @DisplayName("Should return HTTP 400 Bad Request if outletId is missing on active session query")
    void shouldReturnBadRequestWhenOutletIdIsNullOnActiveSession() {
        ResponseEntity<CashSessionResponse> response = cashSessionController.getActiveSession(null, 10L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should submit blind count step 1 and step 2 successfully")
    void shouldSubmitBlindCountStepsSuccessfully() {
        BlindCountStep1Request step1 = new BlindCountStep1Request();
        step1.setEmployeeId(10L);
        step1.setActualCash(new BigDecimal("300.00"));

        ResponseEntity<Void> res1 = cashSessionController.submitBlindCountStep1(50L, step1);
        assertEquals(HttpStatus.OK, res1.getStatusCode());
        verify(cashSessionUseCasePort, times(1)).submitBlindCountStep1(50L, 10L, new BigDecimal("300.00"));

        BlindCountStep2Request step2 = new BlindCountStep2Request();
        step2.setManagerId(5L);
        step2.setManagerCountedCash(new BigDecimal("300.00"));
        step2.setJustification("Conteo verificado por gerente");

        ResponseEntity<Void> res2 = cashSessionController.submitBlindCountStep2(50L, step2);
        assertEquals(HttpStatus.OK, res2.getStatusCode());
        verify(cashSessionUseCasePort, times(1)).submitBlindCountStep2(50L, 5L, new BigDecimal("300.00"), "Conteo verificado por gerente");
    }

    @Test
    @DisplayName("Should register cash pickup/drop and configure threshold successfully")
    void shouldRegisterPickupDropAndThresholdSuccessfully() {
        RegisterCashPickupRequest request = new RegisterCashPickupRequest();
        request.setAmount(new BigDecimal("500.00"));
        request.setReason("Retiro de exceso de efectivo");
        request.setRegisteredBy(5L);

        CashPickupDomain pickup = CashPickupDomain.create(50L, 5L, new BigDecimal("500.00"), "Retiro de exceso de efectivo");

        when(cashSessionUseCasePort.registerPickup(50L, new BigDecimal("500.00"), "Retiro de exceso de efectivo", 5L))
                .thenReturn(pickup);

        ResponseEntity<CashPickupResponse> response = cashSessionController.registerPickup(50L, request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        ConfigureThresholdRequest thresholdRequest = new ConfigureThresholdRequest();
        thresholdRequest.setThresholdAmount(new BigDecimal("800000"));

        ResponseEntity<Void> thresholdRes = cashSessionController.configureThreshold(1L, thresholdRequest);
        assertEquals(HttpStatus.OK, thresholdRes.getStatusCode());
        verify(cashSessionUseCasePort, times(1)).configureThreshold(1L, new BigDecimal("800000"));
    }
}
