package org.frias.avalon.domain.outlet.application.usecase.update;

import org.frias.avalon.domain.outlet.application.dto.request.UpdateDeliverySettingsRequestDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;

public interface UpdateDeliverySettingsUseCase {
    OutletResponseDto execute(Long outletId, UpdateDeliverySettingsRequestDto request);
}
