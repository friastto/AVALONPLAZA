package org.frias.avalon.domain.company.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateCompanyDto(

        Long id,

        String name,

        String nit,

        String address,

        String email




        ) {
}
