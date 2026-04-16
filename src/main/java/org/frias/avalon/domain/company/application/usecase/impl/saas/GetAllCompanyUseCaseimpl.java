package org.frias.avalon.domain.company.application.usecase.impl.saas;

import org.frias.avalon.core.tenant.config.TenantEntity;
import org.frias.avalon.domain.company.application.dtos.response.CompanyWhithMainOutletResponseDto;
import org.frias.avalon.domain.company.application.mappers.CompanyMapper;
import org.frias.avalon.domain.company.application.services.interfaces.CompanyService;
import org.frias.avalon.domain.company.application.usecase.inter.saas.GetAllCompanyUseCase;
import org.frias.avalon.domain.company.domain.entities.Company;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GetAllCompanyUseCaseimpl extends TenantSecurity implements GetAllCompanyUseCase {
    private final CompanyService companyService;
    private final CompanyMapper companyMapper;

    public GetAllCompanyUseCaseimpl(CompanyService companyService, CompanyMapper companyMapper) {
        this.companyService = companyService;
        this.companyMapper = companyMapper;
    }


    @Override
    public List<CompanyWhithMainOutletResponseDto> execute() {

        if (!isMasterStaff()) throw  new SecurityException("no tiene los permisos necesarios para extraer todas las empresas afiliadas en avalon");

        List<Company> companyList = companyService.getAll();

        return companyList == null
                ? List.of()
                : companyList.stream()
                  .map(companyMapper::toDtoCompanyWithMainOutlet)
                  .toList();

    }
}
