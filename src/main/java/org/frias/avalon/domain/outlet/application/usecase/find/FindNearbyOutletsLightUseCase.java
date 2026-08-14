package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.domain.outlet.application.dto.request.OutletNearbyByRadiusRequestDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletLightResponse;

import java.util.List;

/**
 * Input Port / Interface for finding nearby outlets in lightweight response format.
 */
public interface FindNearbyOutletsLightUseCase {

    /**
     * Executes the use case to find nearby outlets by radius.
     *
     * @param request radius search criteria
     * @return list of OutletLightResponse
     */
    List<OutletLightResponse> execute(OutletNearbyByRadiusRequestDto request);
}