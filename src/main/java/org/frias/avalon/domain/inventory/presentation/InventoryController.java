package org.frias.avalon.domain.inventory.presentation;

import jakarta.validation.Valid;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.inventory.application.dto.KardexResponseDto;
import org.frias.avalon.domain.inventory.application.dto.StockAdjustmentRequest;
import org.frias.avalon.domain.inventory.application.dto.StockAdjustmentResponse;
import org.frias.avalon.domain.inventory.application.usecase.GetKardexUseCase;
import org.frias.avalon.domain.inventory.application.usecase.StockAdjustmentUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Pure Clean Architecture REST Controller for store inventory management and Kardex audit ledger.
 */
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final StockAdjustmentUseCase stockAdjustmentUseCase;
    private final GetKardexUseCase getKardexUseCase;

    public InventoryController(
            StockAdjustmentUseCase stockAdjustmentUseCase,
            GetKardexUseCase getKardexUseCase
    ) {
        this.stockAdjustmentUseCase = stockAdjustmentUseCase;
        this.getKardexUseCase = getKardexUseCase;
    }

    /**
     * POST /api/v1/inventory/adjust - Performs a manual stock adjustment.
     */
    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<StockAdjustmentResponse>> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        StockAdjustmentResponse response = stockAdjustmentUseCase.execute(request);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Stock adjusted successfully and Kardex recorded",
                response
        ));
    }

    /**
     * GET /api/v1/inventory/kardex/product/{productOutletId} - Retrieves Kardex ledger for a store product.
     */
    @GetMapping("/kardex/product/{productOutletId}")
    public ResponseEntity<ApiResponse<List<KardexResponseDto>>> getKardexByProduct(@PathVariable Long productOutletId) {
        List<KardexResponseDto> movements = getKardexUseCase.findByProductOutletId(productOutletId);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                movements.isEmpty() ? "No Kardex entries found for product" : "Kardex history retrieved successfully",
                movements
        ));
    }

    /**
     * GET /api/v1/inventory/kardex/outlet/{outletId} - Retrieves Kardex ledger for a store outlet.
     */
    @GetMapping("/kardex/outlet/{outletId}")
    public ResponseEntity<ApiResponse<List<KardexResponseDto>>> getKardexByOutlet(@PathVariable Long outletId) {
        List<KardexResponseDto> movements = getKardexUseCase.findByOutletId(outletId);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                movements.isEmpty() ? "No Kardex entries found for outlet" : "Outlet Kardex history retrieved successfully",
                movements
        ));
    }
}
