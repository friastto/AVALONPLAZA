package org.frias.avalon.domain.claim.infrastructure.persistence.repository;

import org.frias.avalon.domain.claim.infrastructure.persistence.entity.OrderClaimEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOrderClaimRepository extends JpaRepository<OrderClaimEntity, Long> {
    List<OrderClaimEntity> findAllByOrderId(Long orderId);
    List<OrderClaimEntity> findAllByCustomerId(Long customerId);
}
