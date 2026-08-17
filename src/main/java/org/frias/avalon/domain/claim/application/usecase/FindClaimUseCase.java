package org.frias.avalon.domain.claim.application.usecase;

import org.frias.avalon.domain.claim.application.dto.response.ClaimResponse;

import java.util.List;

public interface FindClaimUseCase {
    ClaimResponse findById(Long id);
    List<ClaimResponse> findByOrderId(Long orderId);
}
