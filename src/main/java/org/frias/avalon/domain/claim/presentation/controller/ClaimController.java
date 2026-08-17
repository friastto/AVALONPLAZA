package org.frias.avalon.domain.claim.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.claim.application.dto.request.CreateOrderClaimRequest;
import org.frias.avalon.domain.claim.application.dto.response.ClaimResponse;
import org.frias.avalon.domain.claim.application.usecase.CreateOrderClaimUseCase;
import org.frias.avalon.domain.claim.application.usecase.FindClaimUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final CreateOrderClaimUseCase createOrderClaimUseCase;
    private final FindClaimUseCase findClaimUseCase;

    @PostMapping
    public ResponseEntity<ClaimResponse> createClaim(@Valid @RequestBody CreateOrderClaimRequest request) {
        ClaimResponse response = createOrderClaimUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable Long id) {
        ClaimResponse response = findClaimUseCase.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ClaimResponse>> getClaimsByOrder(@PathVariable Long orderId) {
        List<ClaimResponse> responses = findClaimUseCase.findByOrderId(orderId);
        return ResponseEntity.ok(responses);
    }
}
