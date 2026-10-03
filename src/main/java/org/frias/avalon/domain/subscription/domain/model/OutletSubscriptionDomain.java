package org.frias.avalon.domain.subscription.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model representing a store's subscription in Avalon.
 */
public record OutletSubscriptionDomain(
        Long id,
        Long outletId,
        Long companyId,
        Long statusId,
        Integer billingDay,
        BigDecimal amountCop,
        LocalDateTime trialEndsAt,
        LocalDateTime currentPeriodStart,
        LocalDateTime currentPeriodEnd,
        LocalDateTime gracePeriodEnd,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public OutletSubscriptionDomain {
        if (billingDay == null) {
            billingDay = 28;
        }
        if (amountCop == null) {
            amountCop = new BigDecimal("60000.00");
        }
    }
}
