package org.frias.avalon.domain.company.services.interfaces;

import org.frias.avalon.domain.company.A.CompanyRequestNewDto;
import org.frias.avalon.domain.company.dtos.UpdateCompanyDto;
import org.frias.avalon.domain.company.entities.Company;

public interface CompanyServiceA {

    Company create(CompanyRequestNewDto newCompanyData );
    Company searchByNit(String nit);
    Company searchById(Long id);
    Company update(UpdateCompanyDto request);
    Company updateStatus(Long idCompany, Long IdStatus);

}
