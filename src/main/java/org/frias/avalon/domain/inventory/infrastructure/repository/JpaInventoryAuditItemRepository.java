package org.frias.avalon.domain.inventory.infrastructure.repository;

import org.frias.avalon.domain.inventory.infrastructure.entity.InventoryAuditItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaInventoryAuditItemRepository extends JpaRepository<InventoryAuditItemEntity, Long> {

    List<InventoryAuditItemEntity> findByAuditSessionIdOrderByIdAsc(Long auditSessionId);

    Optional<InventoryAuditItemEntity> findByAuditSessionIdAndProductOutletId(Long auditSessionId, Long productOutletId);
}
