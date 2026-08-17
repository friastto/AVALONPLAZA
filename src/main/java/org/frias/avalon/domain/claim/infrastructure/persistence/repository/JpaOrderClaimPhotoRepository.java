package org.frias.avalon.domain.claim.infrastructure.persistence.repository;

import org.frias.avalon.domain.claim.infrastructure.persistence.entity.OrderClaimPhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOrderClaimPhotoRepository extends JpaRepository<OrderClaimPhotoEntity, Long> {
    List<OrderClaimPhotoEntity> findAllByClaimId(Long claimId);
}
