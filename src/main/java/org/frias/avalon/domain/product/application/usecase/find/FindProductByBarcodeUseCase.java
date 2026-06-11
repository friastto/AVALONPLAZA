package org.frias.avalon.domain.product.application.usecase.find;

import org.frias.avalon.domain.product.application.dto.response.ProductResponse;

/**
 * Use case for finding a product by its barcode.
 */
public interface FindProductByBarcodeUseCase {

    /**
     * Executes the search for a product based on a given barcode.
     *
     * @param barcode The barcode string to search for.
     * @return The ProductResponse DTO if found.
     * @throws org.frias.avalon.core.exeptions.ResourceNotFoundException if the barcode or the associated product is not found.
     */
    ProductResponse execute(String barcode);
}
