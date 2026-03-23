package org.frias.avalon.domain.company.mappers;


import org.frias.avalon.domain.company.A.CompanyResponseDto;
import org.frias.avalon.domain.company.entities.Company;
import org.frias.avalon.temp.empresasucursal.sucursal.mappers.OutletMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
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
