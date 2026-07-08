package org.frias.avalon.domain.product.application.usecase.find;

import org.frias.avalon.domain.product.application.dto.response.ProductResponse;

/**
 * Use case for finding a product by its ID.
 */
public interface FindProductByIdUseCase {

    /**
     * Executes the search for a product based on a given ID.
     *
     * @param productId The ID of the product to search for.
     * @return The ProductResponse DTO if found.
     * @throws org.frias.avalon.core.exeptions.ResourceNotFoundException if the product is not found.
     */
    ProductResponse execute(Long productId);
}
