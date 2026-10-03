package org.frias.avalon.core.exeptions;

/**
 * Thrown when an operation is blocked due to a suspended store subscription.
 * Maps to HTTP 402 Payment Required.
 */
public class SubscriptionSuspendedException extends RuntimeException {

    public SubscriptionSuspendedException(String message) {
        super(message);
    }
}
