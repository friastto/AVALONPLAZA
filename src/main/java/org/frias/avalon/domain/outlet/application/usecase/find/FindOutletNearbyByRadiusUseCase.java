package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.domain.outlet.application.dto.request.OutletNearbyByRadiusRequestDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;

import java.util.List;

public interface FindOutletNearbyByRadiusUseCase {

    List<OutletResponseDto> execute(OutletNearbyByRadiusRequestDto request);
}
