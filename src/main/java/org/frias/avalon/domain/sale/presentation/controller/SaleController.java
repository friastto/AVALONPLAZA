package org.frias.avalon.domain.sale.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.sale.application.dto.request.CreateSaleRequest;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.frias.avalon.domain.sale.application.usecase.sale.create.CreateSaleUseCase;
import org.frias.avalon.domain.sale.application.usecase.sale.find.FindAllSalesUseCase;
import org.frias.avalon.domain.sale.application.usecase.sale.find.FindSaleByCodeUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import org.frias.avalon.domain.sale.application.usecase.sale.find.SearchSalesUseCase;
import java.util.List;

@RestController
@RequestMapping("/avalon/sales")
@RequiredArgsConstructor
public class SaleController {

    private final CreateSaleUseCase createSaleUseCase;
    private final FindSaleByCodeUseCase findSaleByCodeUseCase;
    private final FindAllSalesUseCase findAllSalesUseCase;
    private final SearchSalesUseCase searchSalesUseCase;

    @org.frias.avalon.core.idempotency.Idempotent
    @PostMapping
    public ResponseEntity<ApiResponse<SaleResponse>> createSale(
            @Valid @RequestBody CreateSaleRequest request
    ) {
        SaleResponse response = createSaleUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Venta registrada con éxito", response));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> getRecentSales(
            @RequestParam(required = false) Long outletId
    ) {
        List<SaleResponse> response = searchSalesUseCase.getRecentSales(outletId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Ventas recientes obtenidas", response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> searchSales(
            @RequestParam String query,
            @RequestParam(required = false) Long outletId
    ) {
        List<SaleResponse> response = searchSalesUseCase.search(outletId, query);
        return ResponseEntity.ok(new ApiResponse<>(200, "Búsqueda realizada", response));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<SaleResponse>> getSaleByCode(
            @PathVariable String code,
            @RequestParam(required = false) Long outletId
    ) {
        SaleResponse response = searchSalesUseCase.findByFlexibleCode(code, outletId);
        return ResponseEntity.ok(
                new ApiResponse<>(200, "Venta encontrada", response)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SaleResponse>>> listSales(
            @RequestParam(required = false) Long outletId,
            Pageable pageable
    ) {
        Page<SaleResponse> response = findAllSalesUseCase.execute(outletId, pageable);
        return ResponseEntity.ok(
                new ApiResponse<>(200, "Listado de ventas obtenido con éxito", response)
        );
    }
}
