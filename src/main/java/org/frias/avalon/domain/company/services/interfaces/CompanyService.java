package org.frias.avalon.domain.company.services.interfaces;


import org.frias.avalon.domain.company.dtos.CompanyRequestNewDto;
import org.frias.avalon.domain.company.dtos.CompanyResponseDto;
import org.frias.avalon.domain.company.dtos.UpdateCompanyDto;
import org.frias.avalon.domain.company.entities.Company;

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





}
