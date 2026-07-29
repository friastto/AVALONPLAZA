package org.frias.avalon.domain.cashregister.infrastructure.repository;

import org.frias.avalon.domain.cashregister.infrastructure.entity.CashPickupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaCashPickupRepository extends JpaRepository<CashPickupEntity, Long> {
    List<CashPickupEntity> findBySessionId(Long sessionId);
}
