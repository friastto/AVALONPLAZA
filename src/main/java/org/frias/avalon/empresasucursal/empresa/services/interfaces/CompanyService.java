package org.frias.avalon.empresasucursal.empresa.services.interfaces;


import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.empresasucursal.empresa.dtos.CompanyResponseDto;
import org.frias.avalon.empresasucursal.empresa.dtos.CompanyRequestNewDto;
import org.frias.avalon.empresasucursal.empresa.entities.Company;

public interface CompanyService {

    CompanyResponseDto save(CompanyRequestNewDto empresaNewDto);
    Company findById(Long id);

    CompanyResponseDto searchCompanyAndOutlets(Long id);
    CompanyResponseDto searchCompanyAndOutlets();
    Company searchCompany();

}
