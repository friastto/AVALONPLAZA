package org.frias.avalon.domain.claim.application.usecase;

import org.frias.avalon.domain.claim.application.dto.request.CreateOrderClaimRequest;
import org.frias.avalon.domain.claim.application.dto.response.ClaimResponse;

public interface CreateOrderClaimUseCase {
    ClaimResponse execute(CreateOrderClaimRequest request);
}
