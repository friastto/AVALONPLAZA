package org.frias.avalon.domain.product.application.usecase.find;

import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindProductCatalogByOutletUseCase {
    /**
     * Retrieves a paginated and filtered catalog of products for a specific outlet.
     *
     * @param outletId The ID of the outlet to filter by.
     * @param name Optional name filter.
     * @param categoryId Optional category ID filter.
     * @param pageable Pagination and sorting information.
     * @return A Page containing ProductResponse DTOs.
     */
    Page<ProductResponse> execute(Long outletId, String name, Long categoryId, Pageable pageable);
}
