package org.frias.avalon.domain.company.application.dtos.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.frias.avalon.domain.outlet.dtos.request.OutletNewDto;

import java.util.List;

public record CompanyRequestNewDto(
        @NotBlank(message = "La empresa no piuede estar sin name")

    String name,
        @NotBlank(message = "La empresa debe tener un nit")

    String nit,

        @NotBlank(message = "La empresa debe tener un email de contacto")
        @Email(message = "Debe ingresar un formato de email válido (ejemplo@dominio.com)")
    String email,

        @NotEmpty(message = "La empresa debe tener almenos una sucursal")
        @Valid
    List<OutletNewDto> outlets

){}
