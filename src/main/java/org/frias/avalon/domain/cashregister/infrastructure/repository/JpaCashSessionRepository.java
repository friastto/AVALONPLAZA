package org.frias.avalon.domain.cashregister.infrastructure.repository;

import org.frias.avalon.domain.cashregister.infrastructure.entity.CashSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaCashSessionRepository extends JpaRepository<CashSessionEntity, Long> {

    Optional<CashSessionEntity> findByOutletIdAndEmployeeIdAndStatus(Long outletId, Long employeeId, String status);

    List<CashSessionEntity> findByOutletIdAndStatus(Long outletId, String status);

    List<CashSessionEntity> findByOutletIdOrderByOpenedAtDesc(Long outletId);

    @org.springframework.data.jpa.repository.Query("SELECT c.employeeId FROM CashSessionEntity c WHERE c.outletId = :outletId GROUP BY c.employeeId")
    List<Long> findDistinctEmployeeIdsByOutletId(@org.springframework.data.repository.query.Param("outletId") Long outletId);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM CashSessionEntity c WHERE c.outletId = :outletId " +
           "AND c.difference IS NOT NULL AND c.difference <> 0 " +
           "AND (:employeeId IS NULL OR c.employeeId = :employeeId) " +
           "AND (:discrepancyType IS NULL OR (:discrepancyType = 'SHORTAGE' AND c.difference < 0) OR (:discrepancyType = 'SURPLUS' AND c.difference > 0)) " +
           "AND (:year IS NULL OR YEAR(c.closedAt) = :year) " +
           "AND (:month IS NULL OR MONTH(c.closedAt) = :month) " +
           "AND (:day IS NULL OR DAY(c.closedAt) = :day) " +
           "ORDER BY c.closedAt DESC")
    org.springframework.data.domain.Page<CashSessionEntity> findDiscrepanciesHistory(
            @org.springframework.data.repository.query.Param("outletId") Long outletId,
            @org.springframework.data.repository.query.Param("employeeId") Long employeeId,
            @org.springframework.data.repository.query.Param("discrepancyType") String discrepancyType,
            @org.springframework.data.repository.query.Param("year") Integer year,
            @org.springframework.data.repository.query.Param("month") Integer month,
            @org.springframework.data.repository.query.Param("day") Integer day,
            org.springframework.data.domain.Pageable pageable
    );
}
