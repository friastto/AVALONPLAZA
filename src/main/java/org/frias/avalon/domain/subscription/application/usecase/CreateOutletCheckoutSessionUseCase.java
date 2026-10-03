package org.frias.avalon.domain.subscription.application.usecase;

import org.frias.avalon.domain.subscription.application.dto.response.OutletCheckoutSessionResponseDto;

/**
 * Use case to generate a cryptographically signed checkout session for Wompi.
 */
public interface CreateOutletCheckoutSessionUseCase {
    OutletCheckoutSessionResponseDto execute(Long outletId);
}
