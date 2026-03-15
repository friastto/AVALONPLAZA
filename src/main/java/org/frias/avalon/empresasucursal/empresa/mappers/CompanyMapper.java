package org.frias.avalon.empresasucursal.empresa.mappers;


import org.frias.avalon.empresasucursal.empresa.dtos.CompanyResponseDto;
import org.frias.avalon.empresasucursal.empresa.entities.Company;
import org.frias.avalon.empresasucursal.sucursal.mappers.OutletMapper;
import org.springframework.stereotype.Service;

@Service
public class CompanyMapper {

    public final OutletMapper outletMapper;

    public CompanyMapper(OutletMapper outletMapper) {
        this.outletMapper = outletMapper;
    }

    public CompanyResponseDto toDto(Company company) {

        return new CompanyResponseDto(
                company.getId(),
                company.getNit(),
                company.getName(),
                company.getEmail(),
                outletMapper.listEntityToListDto(company.getOutlets())

        );
    }



}
