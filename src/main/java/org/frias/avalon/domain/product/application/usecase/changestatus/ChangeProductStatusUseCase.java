package org.frias.avalon.domain.product.application.usecase.changestatus;

import org.frias.avalon.domain.product.application.dto.request.ChangeStatusRequest;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;

/**
 * Use case for changing the status of an existing product.
 */
public interface ChangeProductStatusUseCase {

    /**
     * Executes the status change logic.
     *
     * @param productId The ID of the product to update.
     * @param request   The DTO containing the new status ID.
     * @return The updated product as a ProductResponse DTO.
     * @throws org.frias.avalon.core.exeptions.ResourceNotFoundException if the product is not found.
     * @throws org.frias.avalon.core.exeptions.DomainValidationException if the status ID is invalid.
     */
    ProductResponse execute(Long productId, ChangeStatusRequest request);
}
