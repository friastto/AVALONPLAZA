package org.frias.avalon.domain.product.infrastructure.repository;

import org.frias.avalon.domain.product.infrastructure.entity.ProductSuggestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaProductSuggestionRepository extends JpaRepository<ProductSuggestionEntity, Long> {
    List<ProductSuggestionEntity> findByCompanyIdAndStatusOrderByCreatedAtDesc(Long companyId, String status);
    List<ProductSuggestionEntity> findByOutletIdOrderByCreatedAtDesc(Long outletId);
}
