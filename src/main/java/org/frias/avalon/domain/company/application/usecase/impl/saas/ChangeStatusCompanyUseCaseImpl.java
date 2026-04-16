package org.frias.avalon.domain.company.application.usecase.impl.saas;

import org.frias.avalon.domain.company.application.dtos.response.CompanyWhithMainOutletResponseDto;
import org.frias.avalon.domain.company.application.services.interfaces.CompanyService;
import org.frias.avalon.domain.company.application.usecase.inter.saas.ChangeStatusCompanyUseCase;
import org.frias.avalon.domain.company.application.usecase.inter.saas.DeleteCompanyByIdUseCase;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.springframework.stereotype.Service;

@Service
public class ChangeStatusCompanyUseCaseImpl extends TenantSecurity implements ChangeStatusCompanyUseCase {

    private final CompanyService companyService;

    public ChangeStatusCompanyUseCaseImpl(CompanyService companyService) {
        this.companyService = companyService;
    }


    @Override
    public CompanyWhithMainOutletResponseDto execute(Long id, Long idStatus) {

        if(!isMasterStaff()) throw new SecurityException("Usted no esta autorizado para cambiar este estado a una empresa dentro de Avalon");


        companyService.changeStatus(id,idStatus);



        return null;
    }
}
