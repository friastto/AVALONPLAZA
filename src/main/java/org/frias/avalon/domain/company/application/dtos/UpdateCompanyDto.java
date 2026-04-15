package org.frias.avalon.domain.company.application.dtos;

public record UpdateCompanyDto(

        Long id,

        String name,

        String nit,

        String address,

        String email




        ) {
}
