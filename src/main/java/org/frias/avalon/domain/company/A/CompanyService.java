package org.frias.avalon.domain.company.A;


import org.frias.avalon.domain.company.entities.Company;

public interface CompanyService {

    CompanyResponseDto save(CompanyRequestNewDto empresaNewDto);




    Company findById(Long id);

    CompanyResponseDto searchCompanyAndOutlets(Long id);
    CompanyResponseDto searchCompanyAndOutlets();
    Company searchCompany();

}
