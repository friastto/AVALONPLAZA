package org.frias.avalon.domain.product.application.usecase.find;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.application.dto.response.GlobalProductResponseDto;
import org.frias.avalon.domain.product.infraestructure.entity.Product;
import org.frias.avalon.domain.product.infraestructure.repository.JpaGlobalProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para explorar y buscar productos dentro del Catalogo Maestro Global de Avalon (Nivel 1).
 */
@Service
public class FindGlobalProductsUseCaseImpl implements FindGlobalProductsUseCase {

    private final JpaGlobalProductRepository globalProductRepository;
    private final MasterTreeProvider masterTreeProvider;

    public FindGlobalProductsUseCaseImpl(
            JpaGlobalProductRepository globalProductRepository,
            MasterTreeProvider masterTreeProvider
    ) {
        this.globalProductRepository = globalProductRepository;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GlobalProductResponseDto> execute(String query, Pageable pageable) {
        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot actStatus = tree.getByCode("ACT");
        Long activeStatusId = actStatus != null ? actStatus.getId() : null;

        Page<Product> productPage = globalProductRepository.searchGlobalProducts(activeStatusId, query, pageable);

        return productPage.map(product -> new GlobalProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBarcode(),
                MasterRefDto.from(product.getCategoryId() != null ? tree.getById(product.getCategoryId()) : null),
                MasterRefDto.from(product.getUnitMeasureId() != null ? tree.getById(product.getUnitMeasureId()) : null),
                product.getImageUrl(),
                MasterRefDto.from(product.getStatusId() != null ? tree.getById(product.getStatusId()) : null),
                product.getCreatedAt()
        ));
    }
}
