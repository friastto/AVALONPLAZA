package org.frias.avalon.domain.product.application.usecase.find;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindProductCatalogUseCaseImpl implements FindProductCatalogUseCase {

    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final ProductOutletMapper productOutletMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> execute(String name, Pageable pageable) {
        // For the general catalog, we pass a null outletId
        return productOutletRepositoryPort.findAll(name, null, pageable)
                .map(productOutletMapper::toResponse);
    }
}
