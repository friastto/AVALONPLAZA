package org.frias.avalon.domain.subscription.application.usecase;

import org.frias.avalon.domain.subscription.application.dto.response.CompanyOutletsSubscriptionSummaryDto;

/**
 * Use case to retrieve the consolidated subscription status and health of all stores of a company.
 */
public interface FindOutletsSubscriptionStatusByCompanyUseCase {
    CompanyOutletsSubscriptionSummaryDto execute(Long companyId);
}
