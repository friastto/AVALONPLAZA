package org.frias.avalon.domain.order.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.order.application.dto.CreateOrderRequest;
import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.application.dto.UpdateDispatchStatusRequest;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.application.usecase.*;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.mapper.OrderMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final ClaimOrderFifoUseCase claimOrderFifoUseCase;
    private final UpdateItemDispatchStatusUseCase updateItemDispatchStatusUseCase;
    private final CompleteOrderAndEmitSaleUseCase completeOrderAndEmitSaleUseCase;
    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = createOrderUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/claim-next/{outletId}")
    public ResponseEntity<OrderResponse> claimNextOrderFifo(
            @PathVariable Long outletId,
            @RequestParam Long userId) {
        OrderResponse response = claimOrderFifoUseCase.execute(outletId, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/items/{itemId}/dispatch")
    public ResponseEntity<OrderResponse> updateItemDispatchStatus(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateDispatchStatusRequest request) {
        OrderResponse response = updateItemDispatchStatusUseCase.execute(orderId, itemId, request.getStatusId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/complete")
    public ResponseEntity<OrderResponse> completeOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId) {
        OrderResponse response = completeOrderAndEmitSaleUseCase.execute(orderId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/outlet/{outletId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByOutlet(@PathVariable Long outletId) {
        List<OrderDomain> orders = orderRepositoryPort.findAllByOutletId(outletId);
        List<OrderResponse> response = orders.stream().map(orderMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        OrderDomain order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido con ID " + orderId + " no encontrado"));
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }
}
