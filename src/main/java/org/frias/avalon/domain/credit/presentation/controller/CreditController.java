package org.frias.avalon.domain.credit.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.credit.application.dto.request.RegisterPaymentRequest;
import org.frias.avalon.domain.credit.application.dto.request.UpdateCreditLimitRequest;
import org.frias.avalon.domain.credit.application.dto.response.CreditAccountResponse;
import org.frias.avalon.domain.credit.application.dto.response.CreditTransactionResponse;
import org.frias.avalon.domain.credit.application.usecase.find.FindCreditAccountByClientUseCase;
import org.frias.avalon.domain.credit.application.usecase.limit.UpdateCreditLimitUseCase;
import org.frias.avalon.domain.credit.application.usecase.payment.RegisterPaymentUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Rest Controller to expose credit account operations (Fiados) for stores and neighbors.
 */
@RestController
@RequestMapping("/avalon/credit")
@RequiredArgsConstructor
public class CreditController {

    private final FindCreditAccountByClientUseCase findCreditAccountByClientUseCase;
    private final RegisterPaymentUseCase registerPaymentUseCase;
    private final UpdateCreditLimitUseCase updateCreditLimitUseCase;

    /**
     * Finds or initializes a client's credit account in a store.
     *
     * @param clientNumberid The client's unique identification number.
     * @param outletId The store ID.
     * @return The credit account balance details.
     */
    @GetMapping("/client/{clientNumberid}")
    public ResponseEntity<ApiResponse<CreditAccountResponse>> findOrCreateAccount(
            @PathVariable String clientNumberid,
            @RequestParam Long outletId) {
        CreditAccountResponse response = findCreditAccountByClientUseCase.findOrCreate(clientNumberid, outletId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Cuenta de crédito obtenida", response));
    }

    /**
     * Retrieves the history log of payments and purchases for a client.
     *
     * @param clientNumberid The client identification number.
     * @param outletId The store ID.
     * @return The ledger history list.
     */
    @GetMapping("/client/{clientNumberid}/transactions")
    public ResponseEntity<ApiResponse<List<CreditTransactionResponse>>> getAccountTransactions(
            @PathVariable String clientNumberid,
            @RequestParam Long outletId) {
        List<CreditTransactionResponse> responses = findCreditAccountByClientUseCase.findTransactions(clientNumberid, outletId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Historial de transacciones de crédito obtenido", responses));
    }

    /**
     * Registers a payment/installment ("abono") to reduce debt.
     *
     * @param request The payment details.
     * @return The audit transaction response.
     */
    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<CreditTransactionResponse>> registerPayment(
            @Valid @RequestBody RegisterPaymentRequest request) {
        CreditTransactionResponse response = registerPaymentUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Abono registrado exitosamente", response));
    }

    /**
     * Configures a new credit limit threshold for a client account.
     *
     * @param request The credit limit details.
     * @return The updated credit account details.
     */
    @PostMapping("/limit")
    public ResponseEntity<ApiResponse<CreditAccountResponse>> updateLimit(
            @Valid @RequestBody UpdateCreditLimitRequest request) {
        CreditAccountResponse response = updateCreditLimitUseCase.execute(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Límite de crédito actualizado", response));
    }

    /**
     * Lists all active credit accounts (debtors) in a store.
     *
     * @param outletId The store ID.
     * @return The list of credit accounts.
     */
    @GetMapping("/store/{outletId}")
    public ResponseEntity<ApiResponse<List<CreditAccountResponse>>> listStoreDebtors(
            @PathVariable Long outletId) {
        List<CreditAccountResponse> responses = findCreditAccountByClientUseCase.findAllByStore(outletId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lista de deudores de la tienda obtenida", responses));
    }
}
