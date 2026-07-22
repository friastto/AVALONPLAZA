package org.frias.avalon.domain.sale.infrastructure.repository;

import org.frias.avalon.domain.sale.infrastructure.entity.SaleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaSaleRepository extends JpaRepository<SaleEntity, Long> {

    Optional<SaleEntity> findBySaleCode(UUID saleCode);

    Page<SaleEntity> findByOutletId(Long outletId, Pageable pageable);

    @Query("""
        SELECT s FROM SaleEntity s
        JOIN PersonEntity p ON s.clientId = p.id
        WHERE (:outletId IS NULL OR s.outletId = :outletId)
        AND (
            CAST(s.saleCode AS string) LIKE LOWER(CONCAT('%', :query, '%'))
            OR CAST(s.id AS string) = :query
            OR LOWER(p.numberId) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR CAST(s.saleDate AS string) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        ORDER BY s.saleDate DESC
    """)
    java.util.List<SaleEntity> flexibleSearchSales(
            @Param("outletId") Long outletId,
            @Param("query") String query,
            Pageable pageable
    );

    java.util.List<SaleEntity> findTop20ByOutletIdOrderBySaleDateDesc(Long outletId);

    java.util.List<SaleEntity> findByOutletIdAndEmployeeIdAndSaleDateBetween(Long outletId, Long employeeId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    java.util.List<SaleEntity> findByOutletIdAndSaleDateBetween(Long outletId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}
