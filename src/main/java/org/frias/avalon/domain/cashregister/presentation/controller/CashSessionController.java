package org.frias.avalon.domain.cashregister.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.cashregister.application.port.CashSessionUseCasePort;
import org.frias.avalon.domain.cashregister.domain.CashExpenseDomain;
import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;
import org.frias.avalon.domain.cashregister.domain.OutletCashSummaryDomain;
import org.frias.avalon.domain.cashregister.presentation.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cash-sessions")
@RequiredArgsConstructor
public class CashSessionController {

    private final CashSessionUseCasePort cashSessionUseCasePort;

    @PostMapping("/open")
    public ResponseEntity<CashSessionResponse> openSession(@Valid @RequestBody OpenCashSessionRequest request) {
        CashSessionDomain session = cashSessionUseCasePort.openSession(
                request.getOutletId(),
                request.getEmployeeId(),
                request.getInitialBase()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CashSessionResponse.fromDomain(session));
    }

    @PostMapping("/{sessionId}/close")
    public ResponseEntity<CashSessionResponse> closeSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody CloseCashSessionRequest request
    ) {
        CashSessionDomain session = cashSessionUseCasePort.closeSession(
                sessionId,
                request.getActualCash(),
                request.getNotes()
        );
        return ResponseEntity.ok(CashSessionResponse.fromDomain(session));
    }

    @PostMapping("/{sessionId}/expenses")
    public ResponseEntity<CashExpenseDomain> registerExpense(
            @PathVariable Long sessionId,
            @Valid @RequestBody CashExpenseRequest request
    ) {
        CashExpenseDomain expense = cashSessionUseCasePort.registerExpense(
                sessionId,
                request.getAmount(),
                request.getReason(),
                request.getRegisteredBy()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(expense);
    }

    @GetMapping("/active")
    public ResponseEntity<CashSessionResponse> getActiveSession(
            @RequestParam Long outletId,
            @RequestParam Long employeeId
    ) {
        CashSessionDomain session = cashSessionUseCasePort.getActiveSession(outletId, employeeId);
        return ResponseEntity.ok(CashSessionResponse.fromDomain(session));
    }

    @GetMapping("/summary/{outletId}")
    public ResponseEntity<OutletCashSummaryResponse> getOutletSummary(@PathVariable Long outletId) {
        OutletCashSummaryDomain summary = cashSessionUseCasePort.getOutletConsolidatedSummary(outletId);
        return ResponseEntity.ok(OutletCashSummaryResponse.fromDomain(summary));
    }
}
