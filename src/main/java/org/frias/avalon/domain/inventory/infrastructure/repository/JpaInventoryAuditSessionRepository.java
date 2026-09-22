package org.frias.avalon.domain.inventory.infrastructure.repository;

import org.frias.avalon.domain.inventory.infrastructure.entity.InventoryAuditSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaInventoryAuditSessionRepository extends JpaRepository<InventoryAuditSessionEntity, Long> {

    Optional<InventoryAuditSessionEntity> findFirstByOutletIdAndStatus(Long outletId, String status);

    List<InventoryAuditSessionEntity> findByOutletIdOrderByOpenedAtDesc(Long outletId);
}
