package org.frias.avalon.domain.product.infraestructure.adapter;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.frias.avalon.domain.product.infraestructure.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter for product persistence. Implements the output port from the application layer.
 */
@Component
@RequiredArgsConstructor
public class ProductOutletRepositoryAdapter implements ProductOutletRepositoryPort {

    private final JpaProductOutletRepository jpaProductOutletRepository;
    private final ProductOutletMapper productOutletMapper;

    @Override
    public ProductDomain save(ProductDomain productDomain) {
        ProductOutlet entityToSave = productOutletMapper.toEntity(productDomain);
        ProductOutlet savedEntity = jpaProductOutletRepository.save(entityToSave);
        return productOutletMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ProductDomain> findById(Long id) {
        Optional<ProductOutlet> entity = jpaProductOutletRepository.findById(id);
        if (entity.isEmpty()) {
            entity = jpaProductOutletRepository.findByIdFromPublicSchema(id);
        }
        return entity.map(productOutletMapper::toDomain);
    }

    @Override
    public Page<ProductDomain> findAll(String name, Long outletId, Pageable pageable) {
        return findAll(name, outletId, null, pageable);
    }

    @Override
    public Page<ProductDomain> findAll(String name, Long outletId, Long categoryId, Pageable pageable) {
        Specification<ProductOutlet> spec = ProductSpecification.hasName(name)
                .and(ProductSpecification.hasOutletId(outletId))
                .and(ProductSpecification.hasCategoryId(categoryId));

        Page<ProductOutlet> entityPage = jpaProductOutletRepository.findAll(spec, pageable);
        if (entityPage.isEmpty() && outletId != null) {
            entityPage = jpaProductOutletRepository.findFromPublicSchema(outletId, name, pageable);
        }
        return entityPage.map(productOutletMapper::toDomain);
    }
}
