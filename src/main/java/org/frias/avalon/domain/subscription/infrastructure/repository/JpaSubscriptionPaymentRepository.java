package org.frias.avalon.domain.subscription.infrastructure.repository;

import org.frias.avalon.domain.subscription.infrastructure.entity.SubscriptionPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for SubscriptionPaymentEntity.
 */
@Repository
public interface JpaSubscriptionPaymentRepository extends JpaRepository<SubscriptionPaymentEntity, Long> {

    Optional<SubscriptionPaymentEntity> findByWompiReference(String wompiReference);

    Optional<SubscriptionPaymentEntity> findByWompiTransactionId(String wompiTransactionId);

    List<SubscriptionPaymentEntity> findByOutletSubscriptionId(Long outletSubscriptionId);
}
