package org.frias.avalon.domain.credit.infrastructure.repository;

import org.frias.avalon.domain.credit.infrastructure.entity.CreditAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaCreditAccountRepository extends JpaRepository<CreditAccountEntity, Long> {
    Optional<CreditAccountEntity> findByClientIdAndOutletId(Long clientId, Long outletId);
    List<CreditAccountEntity> findAllByOutletId(Long outletId);
}
