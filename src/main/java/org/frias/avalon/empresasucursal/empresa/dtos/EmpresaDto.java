package org.frias.avalon.empresasucursal.empresa.dtos;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.frias.avalon.empresasucursal.sucursal.dtos.OutletRequestNewDto;

import java.util.List;

record EmpresaDto (
        @NotNull(message = "La empresa no piuede estar sin nombre")
    @NotEmpty(message = "La empresa no piuede estar sin nombre")
    String nombre,
        @NotNull(message = "La empresa debe tener un nit")
        @NotEmpty(message = "La empresa debe tener un nit")
    String nit,
        @NotNull(message = "La empresa debe tener un email de contacto")
        @NotEmpty(message = "La empresa debe tener un email de contacto")
    String email,

        @NotNull(message = "La empresa debe tener almenos una sucursal")
        @NotEmpty(message = "La empresa debe tener almenos una sucursal")
    List<OutletRequestNewDto> sucursales

){}
