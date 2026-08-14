package org.frias.avalon.domain.outlet.application.usecase.create;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.port.TenantSchemaMigrationPort;
import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
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

@Service
public class CreateOutletUseCaseImpl implements CreateOutletUseCase {

    private final OutletRepositoryPort outletPort;
    private final MasterDataRepositoryPort masterPort;
    private final MasterTreeProvider masterTreeProvider;
    private final OutletMapper outletMapper;
    private final LocationMapper locationMapper;
    private final TenantSchemaMigrationPort tenantSchemaMigrationPort;

    public CreateOutletUseCaseImpl(OutletRepositoryPort outletPort,
                                 MasterDataRepositoryPort masterPort,
                                 MasterTreeProvider masterTreeProvider,
                                 OutletMapper outletMapper,
                                 LocationMapper locationMapper,
                                 TenantSchemaMigrationPort tenantSchemaMigrationPort) {
        this.outletPort = outletPort;
        this.masterPort = masterPort;
        this.masterTreeProvider = masterTreeProvider;
        this.outletMapper = outletMapper;
        this.locationMapper = locationMapper;
        this.tenantSchemaMigrationPort = tenantSchemaMigrationPort;
    }

    @Transactional
    @Override
    public OutletResponseDto execute(OutletCreateRequestDto dto) {

        MasterRoot status = masterPort.getActiveStatus()
                .orElseThrow(() -> new ResourceNotFoundException("No se pudo activar la tienda en este momento"));

        MasterTree tree = masterTreeProvider.getTree();

        LocationDomain location = new LocationDomain(dto.location().lat(), dto.location().lon());

        OutletDomain outletDomain = OutletDomain.create(
                dto.name(),
                dto.address(),
                dto.phone(),
                dto.nit(),
                status.getId(),
                location,
                java.math.BigDecimal.ZERO,
                dto.companyId()
        );

        OutletDomain outletSaved = outletPort.save(outletDomain);

        // Auto-provision tenant isolated schema in PostgreSQL (e.g. company_1 or store_2)
        if (outletSaved.getCompanyId() != null) {
            tenantSchemaMigrationPort.migrateTenantSchema("company_" + outletSaved.getCompanyId());
        } else {
            tenantSchemaMigrationPort.migrateTenantSchema("store_" + outletSaved.getId());
        }

        StatusResponseDto statusResponse = new StatusResponseDto(status.getId(), status.getShortName(), status.getFullName());

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
                outletSaved.getCompanyId()
        );
    }
}