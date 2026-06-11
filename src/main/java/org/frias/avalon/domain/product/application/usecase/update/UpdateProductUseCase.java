package org.frias.avalon.domain.product.application.usecase.update;

import org.frias.avalon.domain.product.application.dto.request.ProductUpdateRequest;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;

/**
 * Use case for updating an existing product.
 */
public interface UpdateProductUseCase {

    /**
     * Executes the product update logic.
     *
     * @param productId The ID of the product to update.
     * @param request   The DTO containing the new data for the product.
     * @return The updated product as a ProductResponse DTO.
     * @throws org.frias.avalon.core.exeptions.ResourceNotFoundException if the product with the given ID is not found.
     */
    ProductResponse execute(Long productId, ProductUpdateRequest request);
}
