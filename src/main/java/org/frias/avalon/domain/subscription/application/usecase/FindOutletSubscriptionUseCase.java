package org.frias.avalon.domain.subscription.application.usecase;

import org.frias.avalon.domain.subscription.application.dto.response.OutletSubscriptionResponseDto;

public interface FindOutletSubscriptionUseCase {
    OutletSubscriptionResponseDto execute(Long outletId);
}
