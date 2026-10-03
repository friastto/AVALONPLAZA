package org.frias.avalon.domain.subscription.application.usecase;

import org.frias.avalon.domain.subscription.application.dto.request.AcceptPoliciesRequestDto;

/**
 * Use case to accept company policies and commercial terms.
 */
public interface AcceptCompanyPoliciesUseCase {
    void execute(Long companyId, AcceptPoliciesRequestDto request);
}
