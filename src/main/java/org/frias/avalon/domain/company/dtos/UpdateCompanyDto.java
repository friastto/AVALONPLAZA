package org.frias.avalon.domain.company.dtos;

public record UpdateCompanyDto(

        Long id,

        String name,

        String nit,

        String address,

        String email




        ) {
}
