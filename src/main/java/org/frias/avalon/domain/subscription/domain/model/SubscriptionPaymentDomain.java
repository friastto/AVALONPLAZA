package org.frias.avalon.domain.subscription.domain.model;

import java.time.LocalDateTime;

/**
 * Domain model representing a Wompi subscription payment transaction in Avalon.
 */
public record SubscriptionPaymentDomain(
        Long id,
        Long outletSubscriptionId,
        Long outletId,
        Long companyId,
        String wompiTransactionId,
        String wompiReference,
        String paymentMethodType,
        Long amountInCents,
        String currency,
        String status,
        String checksumSent,
        String webhookPayload,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
