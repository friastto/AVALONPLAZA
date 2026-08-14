package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.domain.inventory.application.dto.StockAdjustmentRequest;
import org.frias.avalon.domain.inventory.application.dto.StockAdjustmentResponse;

/**
 * Input Port / Interface for processing inventory stock adjustments.
 */
public interface StockAdjustmentUseCase {

    /**
     * Executes inventory stock adjustment.
     *
     * @param request adjustment details
     * @return StockAdjustmentResponse
     */
    StockAdjustmentResponse execute(StockAdjustmentRequest request);
}
