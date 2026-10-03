package org.frias.avalon.domain.subscription.infrastructure.repository;

import org.frias.avalon.domain.subscription.infrastructure.entity.OutletSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for OutletSubscriptionEntity.
 */
@Repository
public interface JpaOutletSubscriptionRepository extends JpaRepository<OutletSubscriptionEntity, Long> {

    Optional<OutletSubscriptionEntity> findByOutletId(Long outletId);

    List<OutletSubscriptionEntity> findByCompanyId(Long companyId);
}
