package org.frias.avalon.domain.company.application.dtos;


import jakarta.validation.constraints.NotBlank;
import org.frias.avalon.domain.outlet.dtos.response.OutletDto;

record EmpresaDto (

        @NotBlank(message = "La empresa debe tener un nit")
    String nit,

        @NotBlank(message = "La empresa no piuede estar sin name")
    String nombre,



        @NotBlank(message = "La empresa debe tener un email de contacto")
    String email,

    OutletDto outlet

){}
