package org.frias.avalon.domain.cashregister.infrastructure.repository;

import org.frias.avalon.domain.cashregister.infrastructure.entity.CashExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaCashExpenseRepository extends JpaRepository<CashExpenseEntity, Long> {

    List<CashExpenseEntity> findByCashSessionId(Long cashSessionId);

    List<CashExpenseEntity> findByCashSessionIdIn(List<Long> cashSessionIds);
}
