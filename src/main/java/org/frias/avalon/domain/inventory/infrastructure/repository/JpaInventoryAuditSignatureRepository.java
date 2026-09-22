package org.frias.avalon.domain.inventory.infrastructure.repository;

import org.frias.avalon.domain.inventory.infrastructure.entity.InventoryAuditSignatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaInventoryAuditSignatureRepository extends JpaRepository<InventoryAuditSignatureEntity, Long> {

    List<InventoryAuditSignatureEntity> findByAuditSessionId(Long auditSessionId);
}
