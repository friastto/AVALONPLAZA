package org.frias.avalon.domain.cashregister.infrastructure.repository;

import org.frias.avalon.domain.cashregister.infrastructure.entity.CashSessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaCashSessionRepository extends JpaRepository<CashSessionEntity, Long> {

    Optional<CashSessionEntity> findByOutletIdAndEmployeeIdAndStatus(Long outletId, Long employeeId, String status);

    List<CashSessionEntity> findByOutletIdAndStatus(Long outletId, String status);

    List<CashSessionEntity> findByOutletIdOrderByOpenedAtDesc(Long outletId);

    @Query("SELECT c.employeeId FROM CashSessionEntity c WHERE c.outletId = :outletId GROUP BY c.employeeId")
    List<Long> findDistinctEmployeeIdsByOutletId(@Param("outletId") Long outletId);

    @Query("SELECT c FROM CashSessionEntity c WHERE c.outletId = :outletId " +
           "AND c.difference IS NOT NULL AND c.difference <> 0 " +
           "AND (:employeeId IS NULL OR c.employeeId = :employeeId) " +
           "AND (:discrepancyType IS NULL OR (:discrepancyType = 'SHORTAGE' AND c.difference < 0) OR (:discrepancyType = 'SURPLUS' AND c.difference > 0)) " +
           "AND (:year IS NULL OR YEAR(c.closedAt) = :year) " +
           "AND (:month IS NULL OR MONTH(c.closedAt) = :month) " +
           "AND (:day IS NULL OR DAY(c.closedAt) = :day) " +
           "ORDER BY c.closedAt DESC")
    Page<CashSessionEntity> findDiscrepanciesHistory(
            @Param("outletId") Long outletId,
            @Param("employeeId") Long employeeId,
            @Param("discrepancyType") String discrepancyType,
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("day") Integer day,
            Pageable pageable
    );
}
