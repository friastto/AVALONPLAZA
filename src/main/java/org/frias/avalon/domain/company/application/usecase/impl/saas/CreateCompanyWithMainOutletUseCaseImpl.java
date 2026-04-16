package org.frias.avalon.domain.company.application.usecase.impl.saas;

import org.frias.avalon.domain.company.application.dtos.request.CompanyWithOutletDto;
import org.frias.avalon.domain.company.application.dtos.response.CompanyWhithMainOutletResponseDto;
import org.frias.avalon.domain.company.application.mappers.CompanyMapper;
import org.frias.avalon.domain.company.application.services.interfaces.CompanyService;
import org.frias.avalon.domain.company.application.usecase.inter.saas.CreateCompanyWithMainOutletUseCase;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.outlet.dtos.response.OutletDto;
import org.frias.avalon.domain.outlet.mappers.OutletMapper;
import org.frias.avalon.domain.outlet.services.interfaces.OutletService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreateCompanyWithMainOutletUseCaseImpl extends TenantSecurity implements CreateCompanyWithMainOutletUseCase {

    private final CompanyService companyService;
    private final OutletService outletService;
    private final CompanyMapper companyMapper;
    private final OutletMapper outletMapper;

    public CreateCompanyWithMainOutletUseCaseImpl(CompanyService companyService, OutletService outletService, CompanyMapper companyMapper, OutletMapper outletMapper) {
        this.companyService = companyService;
        this.outletService = outletService;
        this.companyMapper = companyMapper;
        this.outletMapper = outletMapper;
    }


    @Override
    public CompanyWhithMainOutletResponseDto execute(CompanyWithOutletDto request) {

        if (!isMasterStaff()) throw new SecurityException("Solo los admin-Avalon Pueden crear una empresa en Avalon");

        CompanyWhithMainOutletResponseDto companyResponse = companyMapper.toDtoCompanyWithMainOutlet(
                companyService.create(
                        request.nit(),
                        request.name(),
                        request.email()
                )
        );

        OutletDto outletresponse = outletMapper.toDto(
                outletService.create(
                        companyResponse.id(),
                        request.outlet().name(),
                        request.outlet().address(),
                        request.outlet().phone(),
                        request.outlet().latitude(),
                        request.outlet().longitude(),
                        true
                )
        );

        return new CompanyWhithMainOutletResponseDto(
                companyResponse.id(),
                companyResponse.nit(),
                companyResponse.name(),
                companyResponse.email(),
                companyResponse.status(),
                outletresponse

        );
    }
}
