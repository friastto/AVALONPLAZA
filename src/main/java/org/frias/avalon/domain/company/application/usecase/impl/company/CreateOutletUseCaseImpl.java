package org.frias.avalon.domain.company.application.usecase.impl.company;

import org.frias.avalon.domain.company.application.usecase.inter.company.CreateOutletUseCase;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.outlet.dtos.request.OutletNewDto;
import org.frias.avalon.domain.outlet.dtos.response.OutletDto;
import org.frias.avalon.domain.outlet.entities.Outlet;
import org.frias.avalon.domain.outlet.mappers.OutletMapper;
import org.frias.avalon.domain.outlet.services.interfaces.OutletService;

public class CreateOutletUseCaseImpl extends TenantSecurity implements CreateOutletUseCase {
    private final OutletService outletService;
    private final OutletMapper outletMapper;

    public CreateOutletUseCaseImpl(OutletService outletService, OutletMapper outletMapper) {
        this.outletService = outletService;
        this.outletMapper = outletMapper;
    }

    @Override
    public OutletDto execute(OutletNewDto request) {

        Long idCompany = getCompanyId();

        if (idCompany == null)
            throw new SecurityException("no tiene los permisos necesarios para crear outlets para esta empresa");


        Outlet o = outletService.create(
                idCompany,
                request.name(),
                request.address(),
                request.phone(),
                request.latitude(),
                request.longitude(),
                false
        );

        return outletMapper.toDto(o);

    }
}
