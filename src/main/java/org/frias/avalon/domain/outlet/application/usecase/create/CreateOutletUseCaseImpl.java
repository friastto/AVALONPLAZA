package org.frias.avalon.domain.outlet.application.usecase.create;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.port.TenantSchemaMigrationPort;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.application.dto.LocationDto;
import org.frias.avalon.domain.outlet.application.dto.request.OutletCreateRequestDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.outlet.infraestructure.mapper.LocationMapper;
import org.frias.avalon.domain.outlet.infraestructure.mapper.OutletMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CreateOutletUseCaseImpl implements CreateOutletUseCase {

    private final OutletRepositoryPort outletPort;
    private final MasterTreeProvider masterTreeProvider;
    private final OutletMapper outletMapper;
    private final LocationMapper locationMapper;
    private final TenantSchemaMigrationPort tenantSchemaMigrationPort;

    public CreateOutletUseCaseImpl(
            OutletRepositoryPort outletPort,
            MasterTreeProvider masterTreeProvider,
            OutletMapper outletMapper,
            LocationMapper locationMapper,
            TenantSchemaMigrationPort tenantSchemaMigrationPort) {
        this.outletPort = outletPort;
        this.masterTreeProvider = masterTreeProvider;
        this.outletMapper = outletMapper;
        this.locationMapper = locationMapper;
        this.tenantSchemaMigrationPort = tenantSchemaMigrationPort;
    }

    @Transactional
    @Override
    public OutletResponseDto execute(OutletCreateRequestDto dto) {

        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot status = tree.getByCodeOrThrow("ACT");

        LocationDomain location = new LocationDomain(dto.location().lat(), dto.location().lon());

        OutletDomain outletDomain = OutletDomain.create(
                dto.name(),
                dto.address(),
                dto.phone(),
                dto.nit(),
                status.getId(),
                location,
                BigDecimal.ZERO,
                dto.companyId()
        );

        OutletDomain outletSaved = outletPort.save(outletDomain);

        // Auto-provision tenant isolated schema in PostgreSQL (e.g. company_1 or store_2)
        if (outletSaved.getCompanyId() != null) {
            tenantSchemaMigrationPort.migrateTenantSchema("company_" + outletSaved.getCompanyId());
        } else {
            tenantSchemaMigrationPort.migrateTenantSchema("store_" + outletSaved.getId());
        }

        MasterRefDto statusResponse = MasterRefDto.from(status);

        LocationDto locationDto = locationMapper.domainToDto(outletSaved.getLocation());

        return new OutletResponseDto(
                outletSaved.getId(),
                outletSaved.getCode(),
                outletSaved.getName(),
                outletSaved.getAddress(),
                outletSaved.getPhone(),
                outletSaved.getNit(),
                locationDto,
                statusResponse,
                outletSaved.getCompanyId(),
                outletSaved.getDeliveryEnabled(),
                outletSaved.getDeliveryFee()
        );
    }
}