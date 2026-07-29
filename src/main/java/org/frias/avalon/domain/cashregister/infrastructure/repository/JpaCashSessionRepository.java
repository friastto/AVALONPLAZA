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
}
