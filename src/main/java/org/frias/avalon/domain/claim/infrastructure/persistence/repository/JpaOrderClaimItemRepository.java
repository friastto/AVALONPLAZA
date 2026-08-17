package org.frias.avalon.domain.claim.infrastructure.persistence.repository;

import org.frias.avalon.domain.claim.infrastructure.persistence.entity.OrderClaimItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOrderClaimItemRepository extends JpaRepository<OrderClaimItemEntity, Long> {
    List<OrderClaimItemEntity> findAllByClaimId(Long claimId);
}
