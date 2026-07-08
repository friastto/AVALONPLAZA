package org.frias.avalon.domain.sale.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.sale.application.dto.request.CreateOrderRequest;
import org.frias.avalon.domain.sale.application.dto.response.OrderResponse;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.frias.avalon.domain.sale.application.usecase.order.create.CreateOrderUseCase;
import org.frias.avalon.domain.sale.application.usecase.order.find.FindOrderByCodeUseCase;
import org.frias.avalon.domain.sale.application.usecase.order.invoice.InvoiceOrderUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/avalon/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final FindOrderByCodeUseCase findOrderByCodeUseCase;
    private final InvoiceOrderUseCase invoiceOrderUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        OrderResponse response = createOrderUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Pedido registrado con éxito", response));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByCode(
            @PathVariable UUID code
    ) {
        OrderResponse response = findOrderByCodeUseCase.execute(code);
        return ResponseEntity.ok(
                new ApiResponse<>(200, "Pedido encontrado", response)
        );
    }

    @PostMapping("/{code}/invoice")
    public ResponseEntity<ApiResponse<SaleResponse>> invoiceOrder(
            @PathVariable UUID code,
            @RequestParam String clientNumberid,
            @RequestParam(required = false) BigDecimal amountReceived
    ) {
        SaleResponse response = invoiceOrderUseCase.execute(code, clientNumberid, amountReceived);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Pedido facturado con éxito", response));
    }
}
