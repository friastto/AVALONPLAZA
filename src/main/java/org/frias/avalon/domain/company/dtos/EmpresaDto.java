package org.frias.avalon.domain.company.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.frias.avalon.temp.empresasucursal.sucursal.dtos.OutletRequestNewDto;

import java.util.List;

record EmpresaDto (

        @NotBlank(message = "La empresa no piuede estar sin nombre")
    String nombre,

        @NotBlank(message = "La empresa debe tener un nit")
    String nit,

        @NotBlank(message = "La empresa debe tener un email de contacto")
    String email,

        @NotBlank(message = "La empresa debe tener una direccion de correspondencia fisica")
    String address

){}
