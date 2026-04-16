package org.frias.avalon.domain.company.application.dtos;


import jakarta.validation.constraints.NotBlank;
import org.frias.avalon.domain.outlet.dtos.response.OutletDto;

record EmpresaDto(

        String nit,

        String nombre,

        String email,

        String address,

        String phone,

        Double latitude,

        Double longitude

) {
}
