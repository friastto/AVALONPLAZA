package org.frias.avalon.domain.sale.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.sale.application.dto.request.CreateReturnRequest;
import org.frias.avalon.domain.sale.application.dto.response.ReturnResponse;
import org.frias.avalon.domain.sale.application.usecase.sale.returns.CreateReturnUseCase;
import org.frias.avalon.domain.sale.application.usecase.sale.returns.FindReturnsUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoints del módulo de Devoluciones / Cambios POS.
 *
 * POST /avalon/returns              → Procesar devolución
 * GET  /avalon/returns/{returnCode} → Consultar devolución por código
 * GET  /avalon/returns?outletId=X   → Listar devoluciones de una tienda
 */
import org.frias.avalon.domain.sale.application.dto.request.CreateExchangeRequest;
import org.frias.avalon.domain.sale.application.dto.response.ExchangeResponse;
import org.frias.avalon.domain.sale.application.usecase.sale.returns.CreateExchangeUseCase;

@RestController
@RequestMapping("/avalon/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final CreateReturnUseCase createReturnUseCase;
    private final CreateExchangeUseCase createExchangeUseCase;
    private final FindReturnsUseCase findReturnsUseCase;

    @org.frias.avalon.core.idempotency.Idempotent
    @PostMapping
    public ResponseEntity<ApiResponse<ReturnResponse>> createReturn(
            @Valid @RequestBody CreateReturnRequest request
    ) {
        ReturnResponse response = createReturnUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Devolución procesada con éxito", response));
    }

    @org.frias.avalon.core.idempotency.Idempotent
    @PostMapping("/exchange")
    public ResponseEntity<ApiResponse<ExchangeResponse>> createExchange(
            @Valid @RequestBody CreateExchangeRequest request
    ) {
        ExchangeResponse response = createExchangeUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Intercambio de productos procesado con éxito", response));
    }

    @GetMapping("/{returnCode}")
    public ResponseEntity<ApiResponse<ReturnResponse>> getByCode(
            @PathVariable UUID returnCode
    ) {
        ReturnResponse response = findReturnsUseCase.findByCode(returnCode)
                .orElseThrow(() -> new org.frias.avalon.core.exeptions.ResourceNotFoundException(
                        "Devolución no encontrada: " + returnCode));
        return ResponseEntity.ok(new ApiResponse<>(200, "Devolución encontrada", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReturnResponse>>> listByOutlet(
            @RequestParam(required = false) Long outletId,
            Pageable pageable
    ) {
        Page<ReturnResponse> response = findReturnsUseCase.findByOutlet(outletId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(200, "Listado de devoluciones", response));
    }
}
