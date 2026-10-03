package org.frias.avalon.domain.subscription.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Consolidated subscription summary for a company and all its store outlets.
 */
public record CompanyOutletsSubscriptionSummaryDto(
        Long companyId,
        String companyName,
        boolean policiesAccepted,
        LocalDateTime policiesAcceptedAt,
        String policiesVersion,
        int totalOutlets,
        int activeOutlets,
        int suspendedOutlets,
        int graceOutlets,
        List<OutletSubscriptionResponseDto> outlets
) {
}
