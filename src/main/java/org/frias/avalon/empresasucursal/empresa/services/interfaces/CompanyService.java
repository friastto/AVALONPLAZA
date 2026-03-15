package org.frias.avalon.empresasucursal.empresa.services.interfaces;


import org.frias.avalon.empresasucursal.empresa.dtos.CompanyResponseDto;
import org.frias.avalon.empresasucursal.empresa.dtos.CompanyRequestNewDto;

public interface CompanyService {

    CompanyResponseDto save(CompanyRequestNewDto empresaNewDto);

    CompanyResponseDto searchCompanyAndOutlets(Long id);
    CompanyResponseDto searchCompanyAndOutlets();
}
