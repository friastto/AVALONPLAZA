package org.frias.avalon.domain.company.application.usecase.impl.saas;

import org.frias.avalon.domain.company.application.dtos.response.CompanyWhithMainOutletResponseDto;
import org.frias.avalon.domain.company.application.mappers.CompanyMapper;
import org.frias.avalon.domain.company.application.services.interfaces.CompanyService;
import org.frias.avalon.domain.company.application.usecase.inter.saas.DeleteCompanyByIdUseCase;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.springframework.stereotype.Service;

@Service
public class DeleteCompanyUseCaseImpl extends TenantSecurity implements DeleteCompanyByIdUseCase {

    private final CompanyService companyService;
    private final CompanyMapper companyMapper;

    public DeleteCompanyUseCaseImpl(CompanyService companyService, CompanyMapper companyMapper) {
        this.companyService = companyService;
        this.companyMapper = companyMapper;
    }


    @Override
    public CompanyWhithMainOutletResponseDto execute(Long id) {

        if(!isMasterStaff()) throw new SecurityException("Usted no esta autorizado para eliminar a una empresa dentro de Avalon");

        return companyMapper.toDtoCompanyWithMainOutlet(companyService.disableCompany(id));

    }
}
