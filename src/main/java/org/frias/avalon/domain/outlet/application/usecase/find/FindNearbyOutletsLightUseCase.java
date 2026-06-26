package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.domain.outlet.application.dto.request.OutletNearbyByRadiusRequestDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletLightResponse;
import org.frias.avalon.domain.outlet.domain.model.OutletLocationInfo;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FindNearbyOutletsLightUseCase {

    private final OutletRepositoryPort outletRepository;

    public FindNearbyOutletsLightUseCase(OutletRepositoryPort outletRepository) {
        this.outletRepository = outletRepository;
    }

    public List<OutletLightResponse> execute(OutletNearbyByRadiusRequestDto request) {
        List<OutletLocationInfo> outlets = outletRepository.findNearbyByRadiusLight(
                request.location().lat(),
                request.location().lon(),
                request.radius()
        );

        return outlets.stream()
                .map(this::toLightResponse)
                .collect(Collectors.toList());
    }

    private OutletLightResponse toLightResponse(OutletLocationInfo domain) {
        return new OutletLightResponse(
                domain.id(),
                domain.name(),
                domain.latitude(),
                domain.longitude()
        );
    }
}