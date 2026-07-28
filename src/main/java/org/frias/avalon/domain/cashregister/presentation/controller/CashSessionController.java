package org.frias.avalon.domain.cashregister.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.idempotency.Idempotent;
import org.frias.avalon.domain.cashregister.application.port.CashSessionUseCasePort;
import org.frias.avalon.domain.cashregister.domain.CashExpenseDomain;
import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;
import org.frias.avalon.domain.cashregister.domain.OutletCashSummaryDomain;
import org.frias.avalon.domain.cashregister.presentation.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cash-sessions")
@RequiredArgsConstructor
public class CashSessionController {

    private final CashSessionUseCasePort cashSessionUseCasePort;

    @Idempotent
    @PostMapping("/open")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN', 'CJPRINCIPAL', 'CJTURNO')")
    public ResponseEntity<CashSessionResponse> openSession(@Valid @RequestBody OpenCashSessionRequest request) {
        CashSessionDomain session = cashSessionUseCasePort.openSession(
                request.getOutletId(),
                request.getEmployeeId(),
                request.getInitialBase()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CashSessionResponse.fromDomain(session));
    }

    @Idempotent
    @PostMapping("/{sessionId}/close")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN', 'CJPRINCIPAL', 'CJTURNO')")
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

    @Idempotent
    @PostMapping("/{sessionId}/audit-three-step")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN', 'CJPRINCIPAL', 'CJTURNO')")
    public ResponseEntity<CashSessionResponse> submitThreeStepAudit(
            @PathVariable Long sessionId,
            @RequestBody ThreeStepAuditRequest request
    ) {
        CashSessionDomain session = cashSessionUseCasePort.submitThreeStepAudit(
                sessionId,
                request.getBaseCash(),
                request.getRemainingCash(),
                request.getNotes()
        );
        return ResponseEntity.ok(CashSessionResponse.fromDomain(session));
    }

    @Idempotent
    @PostMapping("/{sessionId}/expenses")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN', 'CJPRINCIPAL', 'CJTURNO')")
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
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN', 'CJPRINCIPAL', 'CJTURNO')")
    public ResponseEntity<CashSessionResponse> getActiveSession(
            @RequestParam(required = false) Long outletId,
            @RequestParam(required = false) Long employeeId
    ) {
        if (outletId == null) {
            return ResponseEntity.badRequest().build();
        }
        CashSessionResponse response = cashSessionUseCasePort.getActiveSessionResponse(outletId, employeeId != null ? employeeId : 0L);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary/{outletId}")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN', 'CJPRINCIPAL', 'CJTURNO')")
    public ResponseEntity<OutletCashSummaryResponse> getOutletSummary(@PathVariable Long outletId) {
        OutletCashSummaryResponse response = cashSessionUseCasePort.getOutletConsolidatedSummaryResponse(outletId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/thresholds")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN', 'CJPRINCIPAL', 'CJTURNO')")
    public ResponseEntity<ThresholdConfigurationResponse> getThresholds(@RequestParam(required = false) Long outletId) {
        return ResponseEntity.ok(ThresholdConfigurationResponse.builder()
                .warningThreshold(new BigDecimal("500000"))
                .blockThreshold(new BigDecimal("1000000"))
                .build());
    }

    @PutMapping("/outlets/{outletId}/threshold")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN')")
    public ResponseEntity<Void> configureThreshold(
            @PathVariable Long outletId,
            @Valid @RequestBody ConfigureThresholdRequest request
    ) {
        cashSessionUseCasePort.configureThreshold(outletId, request.getThresholdAmount());
        return ResponseEntity.ok().build();
    }

    @Idempotent
    @PostMapping("/{sessionId}/pickups")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN')")
    public ResponseEntity<CashPickupResponse> registerPickup(
            @PathVariable Long sessionId,
            @Valid @RequestBody RegisterCashPickupRequest request
    ) {
        org.frias.avalon.domain.cashregister.domain.CashPickupDomain pickup = cashSessionUseCasePort.registerPickup(
                sessionId,
                request.getAmount(),
                request.getReason(),
                request.getRegisteredBy()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CashPickupResponse.fromDomain(pickup));
    }

    @Idempotent
    @PostMapping("/{sessionId}/drops")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN')")
    public ResponseEntity<CashPickupResponse> registerDrop(
            @PathVariable Long sessionId,
            @Valid @RequestBody RegisterCashPickupRequest request
    ) {
        return registerPickup(sessionId, request);
    }

    @PostMapping("/{sessionId}/blind-count/step-1")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN', 'CJPRINCIPAL', 'CJTURNO')")
    public ResponseEntity<Void> submitBlindCountStep1(
            @PathVariable Long sessionId,
            @Valid @RequestBody BlindCountStep1Request request
    ) {
        cashSessionUseCasePort.submitBlindCountStep1(sessionId, request.getEmployeeId(), request.getActualCash());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sessionId}/blind-count/step-2")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN')")
    public ResponseEntity<Void> submitBlindCountStep2(
            @PathVariable Long sessionId,
            @Valid @RequestBody BlindCountStep2Request request
    ) {
        cashSessionUseCasePort.submitBlindCountStep2(sessionId, request.getManagerId(), request.getManagerCountedCash(), request.getJustification());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/discrepancies")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN')")
    public ResponseEntity<List<DiscrepancyAuditResponse>> getDiscrepancies(@RequestParam(required = false) Long outletId) {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/audit/discrepancies")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN')")
    public ResponseEntity<List<DiscrepancyAuditResponse>> getAuditDiscrepancies(@RequestParam(required = false) Long outletId) {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{sessionId}/discrepancies")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN')")
    public ResponseEntity<List<DiscrepancyAuditResponse>> getSessionDiscrepancies(
            @PathVariable Long sessionId,
            @RequestParam(required = false) Long outletId
    ) {
        return ResponseEntity.ok(List.of());
    }
}
