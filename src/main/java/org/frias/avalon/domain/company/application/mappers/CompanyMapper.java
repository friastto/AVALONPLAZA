package org.frias.avalon.domain.company.application.mappers;


import org.antlr.v4.runtime.atn.SemanticContext;
import org.frias.avalon.domain.company.application.dtos.CompanyResponseDto;
import org.frias.avalon.domain.company.application.dtos.response.CompanyWhithMainOutletResponseDto;
import org.frias.avalon.domain.company.domain.entities.Company;
import org.frias.avalon.domain.outlet.dtos.response.OutletDto;
import org.frias.avalon.domain.outlet.mappers.OutletMapper;
import org.springframework.stereotype.Component;

import java.util.List;

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


    public CompanyWhithMainOutletResponseDto toDtoCompanyWithMainOutlet(Company c) {

       List<OutletDto> oDto ;

       if (!c.getOutlets().isEmpty()){

           oDto = c.getOutlets().stream()
                   .map(outletMapper::toDto)
                   .toList();
       }else {

           oDto = List.of();
       }

        return new CompanyWhithMainOutletResponseDto(
                c.getId(),
                c.getNit(),
                c.getName(),
                c.getEmail(),
                oDto
        );


    }
}
