package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.application.dto.LocationDto;
import org.frias.avalon.domain.outlet.application.dto.request.OutletNearbyByRadiusRequestDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.outlet.infraestructure.mapper.LocationMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service

public class FindOutletNearbyByRadiusUseCaseImpl implements FindOutletNearbyByRadiusUseCase {

    private final OutletRepositoryPort outletPort;
    private final MasterDataRepositoryPort masterPort;
    private final LocationMapper locationMapper;
    private final MasterTreeProvider masterTreeProvider;

    public FindOutletNearbyByRadiusUseCaseImpl(OutletRepositoryPort outletPort, MasterDataRepositoryPort masterPort, LocationMapper locationMapper, MasterTreeProvider masterTreeProvider) {
        this.outletPort = outletPort;
        this.masterPort = masterPort;
        this.locationMapper = locationMapper;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    public List<OutletResponseDto> execute(OutletNearbyByRadiusRequestDto request) {


        LocationDomain location = locationMapper.dtoToDomain(request.location());

        List<OutletDomain> outletFind = outletPort.findNearbyByRadius(location, request.radius());

        MasterTree tree = masterTreeProvider.getTree();

        return outletFind.stream().map(outletDomain -> {
                    MasterRoot status = tree.getById(outletDomain.getStatusId());

                    LocationDto currentLocation = locationMapper.domainToDto(outletDomain.getLocation());

                    return new OutletResponseDto(
                            outletDomain.getId(),
                            outletDomain.getCode(),
                            outletDomain.getName(),
                            outletDomain.getAddress(),
                            outletDomain.getPhone(),
                            outletDomain.getNit(),
                            currentLocation,
                            new StatusResponseDto(status.getId(), status.getShortName(), status.getFullName()),
                            outletDomain.getCompanyId(),
                            outletDomain.getDeliveryEnabled(),
                            outletDomain.getDeliveryFee()
                    );
                }
        ).collect(Collectors.toList());

    }
}
