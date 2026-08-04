package org.frias.avalon.domain.inventory.presentation;

import jakarta.validation.Valid;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.inventory.application.dto.StockAdjustmentRequest;
import org.frias.avalon.domain.inventory.application.usecase.StockAdjustmentUseCaseImpl;
import org.frias.avalon.domain.inventory.infrastructure.entity.StockMovementEntity;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaStockMovementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for store inventory management and Kardex audit ledger.
 * Exposes endpoints under /api/v1/inventory for stock adjustments and Kardex history.
 */
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final StockAdjustmentUseCaseImpl stockAdjustmentUseCase;
    private final JpaStockMovementRepository stockMovementRepository;

    public InventoryController(
            StockAdjustmentUseCaseImpl stockAdjustmentUseCase,
            JpaStockMovementRepository stockMovementRepository
    ) {
        this.stockAdjustmentUseCase = stockAdjustmentUseCase;
        this.stockMovementRepository = stockMovementRepository;
    }

    /**
     * POST /api/v1/inventory/adjust - Performs a manual stock adjustment.
     * Updates physical stock immediately (store manager autonomy) and logs immutable Kardex entry.
     *
     * @param request StockAdjustmentRequest DTO
     * @return ResponseEntity with success status
     */
    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<String>> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        stockAdjustmentUseCase.execute(request);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Stock adjusted successfully and Kardex recorded",
                "SUCCESS"
        ));
    }

    /**
     * GET /api/v1/inventory/kardex/product/{productOutletId} - Retrieves Kardex ledger for a store product.
     *
     * @param productOutletId Store product identifier
     * @return List of immutable Kardex movement entries
     */
    @GetMapping("/kardex/product/{productOutletId}")
    public ResponseEntity<ApiResponse<List<StockMovementEntity>>> getKardexByProduct(@PathVariable Long productOutletId) {
        List<StockMovementEntity> movements = stockMovementRepository.findByProductOutletIdOrderByCreatedAtDesc(productOutletId);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                movements.isEmpty() ? "No Kardex entries found for product" : "Kardex history retrieved successfully",
                movements
        ));
    }

    /**
     * GET /api/v1/inventory/kardex/outlet/{outletId} - Retrieves Kardex ledger for a store outlet.
     *
     * @param outletId Store outlet identifier
     * @return List of store Kardex movement entries
     */
    @GetMapping("/kardex/outlet/{outletId}")
    public ResponseEntity<ApiResponse<List<StockMovementEntity>>> getKardexByOutlet(@PathVariable Long outletId) {
        List<StockMovementEntity> movements = stockMovementRepository.findByOutletIdOrderByCreatedAtDesc(outletId);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                movements.isEmpty() ? "No Kardex entries found for outlet" : "Outlet Kardex history retrieved successfully",
                movements
        ));
    }
}
