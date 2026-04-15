package org.frias.avalon.domain.company.application.services.interfaces;


import org.frias.avalon.domain.company.application.dtos.request.CompanyRequestNewDto;
import org.frias.avalon.domain.company.application.dtos.CompanyResponseDto;
import org.frias.avalon.domain.company.application.dtos.UpdateCompanyDto;
import org.frias.avalon.domain.company.domain.entities.Company;

public interface CompanyService {

    Company create(CompanyRequestNewDto newCompanyData );
    

    CompanyResponseDto createCompanyWhitOutlets(CompanyRequestNewDto empresaNewDto);

    Company searchById(Long id);
    Company searchByNit(String nit);
    Company update(UpdateCompanyDto request);
    Company updateStatus(Long idCompany, Long IdStatus);



    CompanyResponseDto searchCompanyAndOutlets(Long id);
    CompanyResponseDto searchCompanyAndOutlets();
    Company searchCompany();


    Company create(
            String nit,
            String name,
            String email
    );
}
