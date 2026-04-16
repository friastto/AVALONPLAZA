package org.frias.avalon.domain.company.application.usecase.impl.saas;

import org.frias.avalon.domain.company.application.dtos.response.CompanyWhithMainOutletResponseDto;
import org.frias.avalon.domain.company.application.mappers.CompanyMapper;
import org.frias.avalon.domain.company.application.services.interfaces.CompanyService;
import org.frias.avalon.domain.company.application.usecase.inter.saas.SearchCompanyUseCase;
import org.frias.avalon.domain.company.domain.entities.Company;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.springframework.stereotype.Service;

@Service
public class SearchCompanyUseCaseimpl extends TenantSecurity implements SearchCompanyUseCase {

    private final CompanyService companyService;
    private final CompanyMapper companyMapper;

    public SearchCompanyUseCaseimpl(CompanyService companyService, CompanyMapper companyMapper) {
        this.companyService = companyService;
        this.companyMapper = companyMapper;
    }


    @Override
    public CompanyWhithMainOutletResponseDto execute(Long id) {

      Company company = companyService.searchCompany(id);

       return companyMapper.toDtoCompanyWithMainOutlet(company);
    }
}
