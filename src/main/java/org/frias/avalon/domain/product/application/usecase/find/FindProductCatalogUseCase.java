package org.frias.avalon.domain.product.application.usecase.find;

import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindProductCatalogUseCase {
    /**
     * Retrieves a paginated and filtered catalog of products.
     *
     * @param name Optional name filter.
     * @param pageable Pagination and sorting information.
     * @return A Page containing ProductResponse DTOs.
     */
    Page<ProductResponse> execute(String name, Pageable pageable);
}
