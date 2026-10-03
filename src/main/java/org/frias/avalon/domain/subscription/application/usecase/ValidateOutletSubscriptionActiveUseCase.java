package org.frias.avalon.domain.subscription.application.usecase;

/**
 * Use case to validate that an outlet's subscription allows mutator operations (POS sales, open cash register, orders).
 * Throws SubscriptionSuspendedException (HTTP 402) if the store is suspended.
 */
public interface ValidateOutletSubscriptionActiveUseCase {
    void execute(Long outletId);
}
