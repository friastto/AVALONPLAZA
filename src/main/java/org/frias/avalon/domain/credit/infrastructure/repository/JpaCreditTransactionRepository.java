package org.frias.avalon.domain.credit.infrastructure.repository;

import org.frias.avalon.domain.credit.infrastructure.entity.CreditTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JpaCreditTransactionRepository extends JpaRepository<CreditTransactionEntity, Long> {
    List<CreditTransactionEntity> findAllByCreditAccountIdOrderByCreatedAtDesc(Long creditAccountId);
}
