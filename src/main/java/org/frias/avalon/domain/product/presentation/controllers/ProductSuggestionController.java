package org.frias.avalon.domain.product.presentation.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.product.infrastructure.entity.ProductSuggestionEntity;
import org.frias.avalon.domain.product.infrastructure.repository.JpaProductSuggestionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avalon/products/suggestions")
@RequiredArgsConstructor
public class ProductSuggestionController {

    private final JpaProductSuggestionRepository suggestionRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductSuggestionEntity>> createSuggestion(@Valid @RequestBody ProductSuggestionEntity suggestion) {
        suggestion.setStatus("PENDING");
        ProductSuggestionEntity saved = suggestionRepository.save(suggestion);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Sugerencia de producto enviada a gerencia exitosamente", saved));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<ProductSuggestionEntity>>> getPendingSuggestionsByCompany(@PathVariable Long companyId) {
        List<ProductSuggestionEntity> pending = suggestionRepository.findByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, "PENDING");
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Sugerencias pendientes obtenidas exitosamente", pending));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ProductSuggestionEntity>> approveSuggestion(@PathVariable Long id) {
        ProductSuggestionEntity suggestion = suggestionRepository.findById(id).orElse(null);
        if (suggestion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Sugerencia no encontrada", null));
        }
        suggestion.setStatus("APPROVED");
        ProductSuggestionEntity updated = suggestionRepository.save(suggestion);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Sugerencia aprobada exitosamente y propagada a las tiendas", updated));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ProductSuggestionEntity>> rejectSuggestion(@PathVariable Long id, @RequestParam(required = false) String reason) {
        ProductSuggestionEntity suggestion = suggestionRepository.findById(id).orElse(null);
        if (suggestion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Sugerencia no encontrada", null));
        }
        suggestion.setStatus("REJECTED");
        suggestion.setRejectionReason(reason != null ? reason : "Rechazado por gerencia");
        ProductSuggestionEntity updated = suggestionRepository.save(suggestion);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Sugerencia rechazada exitosamente", updated));
    }
}
