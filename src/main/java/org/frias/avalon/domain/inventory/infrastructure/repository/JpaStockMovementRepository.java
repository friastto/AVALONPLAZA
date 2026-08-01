package org.frias.avalon.domain.inventory.infrastructure.repository;

import org.frias.avalon.domain.inventory.infrastructure.entity.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for stock_movement Kardex table.
 */
public interface JpaStockMovementRepository extends JpaRepository<StockMovementEntity, Long> {

    /** Returns the full Kardex history for a given product-outlet record. */
    List<StockMovementEntity> findByProductOutletIdOrderByCreatedAtDesc(Long productOutletId);

    /** Returns all movements for a given outlet, latest first. */
    List<StockMovementEntity> findByOutletIdOrderByCreatedAtDesc(Long outletId);
}
