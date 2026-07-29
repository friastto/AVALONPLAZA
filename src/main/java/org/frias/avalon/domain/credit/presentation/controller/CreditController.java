package org.frias.avalon.domain.credit.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.core.idempotency.Idempotent;
import org.frias.avalon.domain.credit.application.dto.request.RegisterPaymentRequest;
import org.frias.avalon.domain.credit.application.dto.request.UpdateCreditLimitRequest;
import org.frias.avalon.domain.credit.application.dto.response.CreditAccountResponse;
import org.frias.avalon.domain.credit.application.dto.response.CreditTransactionResponse;
import org.frias.avalon.domain.credit.application.usecase.find.FindCreditAccountByClientUseCase;
import org.frias.avalon.domain.credit.application.usecase.limit.UpdateCreditLimitUseCase;
import org.frias.avalon.domain.credit.application.usecase.payment.RegisterPaymentUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para la gestión de cuentas de crédito y fiados.
 */
@RestController
@RequestMapping("/avalon/credit")
@RequiredArgsConstructor
public class CreditController {

    private final FindCreditAccountByClientUseCase findCreditAccountByClientUseCase;
    private final RegisterPaymentUseCase registerPaymentUseCase;
    private final UpdateCreditLimitUseCase updateCreditLimitUseCase;

    /**
     * Consulta o inicializa la cuenta de crédito de un cliente en una tienda.
     */
    @GetMapping("/client/{clientNumberid}")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN', 'CJPRINCIPAL', 'CJTURNO')")
    public ResponseEntity<ApiResponse<CreditAccountResponse>> findOrCreateAccount(
            @PathVariable String clientNumberid,
            @RequestParam Long outletId) {
        CreditAccountResponse response = findCreditAccountByClientUseCase.findOrCreate(clientNumberid, outletId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Cuenta de crédito obtenida", response));
    }

    /**
     * Obtiene el historial de transacciones (abonos y consumos).
     */
    @GetMapping("/client/{clientNumberid}/transactions")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN', 'CJPRINCIPAL', 'CJTURNO')")
    public ResponseEntity<ApiResponse<List<CreditTransactionResponse>>> getAccountTransactions(
            @PathVariable String clientNumberid,
            @RequestParam Long outletId) {
        List<CreditTransactionResponse> responses = findCreditAccountByClientUseCase.findTransactions(clientNumberid, outletId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Historial de transacciones de crédito obtenido", responses));
    }

    /**
     * Registra un abono/pago de deuda (Operación financiera idempotente).
     */
    @Idempotent
    @PostMapping("/pay")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN', 'CJPRINCIPAL', 'CJTURNO')")
    public ResponseEntity<ApiResponse<CreditTransactionResponse>> registerPayment(
            @Valid @RequestBody RegisterPaymentRequest request) {
        CreditTransactionResponse response = registerPaymentUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Abono registrado exitosamente", response));
    }

    /**
     * Modifica el límite de crédito de una cuenta (Exclusivo Gerente / Admin).
     */
    @PostMapping("/limit")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN')")
    public ResponseEntity<ApiResponse<CreditAccountResponse>> updateLimit(
            @Valid @RequestBody UpdateCreditLimitRequest request) {
        CreditAccountResponse response = updateCreditLimitUseCase.execute(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Límite de crédito actualizado", response));
    }

    /**
     * Lista todas las cuentas de crédito activas (deudores) de una tienda.
     */
    @GetMapping("/store/{outletId}")
    @PreAuthorize("hasAnyRole('ADMINTI', 'ADMIN', 'GERGEN', 'CJPRINCIPAL')")
    public ResponseEntity<ApiResponse<List<CreditAccountResponse>>> listStoreDebtors(
            @PathVariable Long outletId) {
        List<CreditAccountResponse> responses = findCreditAccountByClientUseCase.findAllByStore(outletId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lista de deudores de la tienda obtenida", responses));
    }
}
