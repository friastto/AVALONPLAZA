package org.frias.avalon.domain.subscription.domain.port;

import org.frias.avalon.domain.subscription.domain.model.OutletSubscriptionDomain;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for OutletSubscription domain operations.
 */
public interface OutletSubscriptionRepositoryPort {

    OutletSubscriptionDomain save(OutletSubscriptionDomain subscription);

    Optional<OutletSubscriptionDomain> findById(Long id);

    Optional<OutletSubscriptionDomain> findByOutletId(Long outletId);

    List<OutletSubscriptionDomain> findByCompanyId(Long companyId);

    List<OutletSubscriptionDomain> findAll();
}
