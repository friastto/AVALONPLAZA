package org.frias.avalon.domain.product.infrastructure.repository;

import org.frias.avalon.domain.product.infrastructure.entity.ProductCompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Level 2 product_company catalog entries.
 */
@Repository
public interface JpaProductCompanyRepository extends JpaRepository<ProductCompanyEntity, Long> {

    Optional<ProductCompanyEntity> findByProductIdAndCompanyId(Long productId, Long companyId);

    List<ProductCompanyEntity> findByCompanyId(Long companyId);

    boolean existsByProductIdAndCompanyId(Long productId, Long companyId);
}
