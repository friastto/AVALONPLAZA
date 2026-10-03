package org.frias.avalon.domain.subscription.application.dto.response;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Output DTO for store subscription status and lifecycle metrics.
 */
public record OutletSubscriptionResponseDto(
        Long id,
        Long outletId,
        String outletName,
        Long companyId,
        MasterRefDto status,
        Integer billingDay,
        BigDecimal amountCop,
        LocalDateTime trialEndsAt,
        LocalDateTime currentPeriodStart,
        LocalDateTime currentPeriodEnd,
        LocalDateTime gracePeriodEnd,
        Long daysUntilDue,
        boolean isDueSoon,
        boolean isInGracePeriod,
        boolean isSuspended,
        boolean canOperate
) {
}
