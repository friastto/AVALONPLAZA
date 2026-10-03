package org.frias.avalon.domain.subscription.domain.port;

import org.frias.avalon.domain.subscription.domain.model.SubscriptionPaymentDomain;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for SubscriptionPayment domain operations.
 */
public interface SubscriptionPaymentRepositoryPort {

    SubscriptionPaymentDomain save(SubscriptionPaymentDomain payment);

    Optional<SubscriptionPaymentDomain> findById(Long id);

    Optional<SubscriptionPaymentDomain> findByWompiReference(String wompiReference);

    Optional<SubscriptionPaymentDomain> findByWompiTransactionId(String wompiTransactionId);

    List<SubscriptionPaymentDomain> findByOutletSubscriptionId(Long outletSubscriptionId);
}
