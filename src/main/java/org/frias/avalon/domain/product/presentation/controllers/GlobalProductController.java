package org.frias.avalon.domain.product.presentation.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.product.application.dto.request.AdoptGlobalProductRequestDto;
import org.frias.avalon.domain.product.application.dto.response.GlobalProductResponseDto;
import org.frias.avalon.domain.product.application.dto.response.ProductSuggestionResponseDto;
import org.frias.avalon.domain.product.application.usecase.adopt.AdoptGlobalProductUseCase;
import org.frias.avalon.domain.product.application.usecase.find.FindGlobalProductsUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para el Catalogo Global Maestro de Avalon (Nivel 1)
 * y su adopcion hacia el Catalogo de Empresa (Nivel 2).
 */
@RestController
@RequestMapping("/avalon/products")
@RequiredArgsConstructor
public class GlobalProductController {

    private final FindGlobalProductsUseCase findGlobalProductsUseCase;
    private final AdoptGlobalProductUseCase adoptGlobalProductUseCase;

    @GetMapping("/global")
    public ResponseEntity<ApiResponse<Page<GlobalProductResponseDto>>> getGlobalCatalog(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GlobalProductResponseDto> page = findGlobalProductsUseCase.execute(query, pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Catalogo maestro global obtenido exitosamente", page));
    }

    @PostMapping("/company/adopt")
    public ResponseEntity<ApiResponse<ProductSuggestionResponseDto>> adoptGlobalProduct(
            @Valid @RequestBody AdoptGlobalProductRequestDto request) {
        ProductSuggestionResponseDto response = adoptGlobalProductUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Producto adoptado por la empresa y propagado a todas sus tiendas exitosamente", response));
    }
}
