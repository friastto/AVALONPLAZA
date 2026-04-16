package org.frias.avalon.domain.company.application.dtos.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.frias.avalon.domain.outlet.dtos.request.OutletNewDto;
import org.frias.avalon.domain.outlet.dtos.response.OutletDto;

public record CompanyWithOutletDto(

        @NotBlank(message = "La empresa debe tener un nit")
        String nit,

        @NotBlank(message = "La empresa no piuede estar sin name")
        String name,

        @NotBlank(message = "La empresa debe tener un email de contacto")
        String email,

        @NotNull(message = "La empresa debe tener una outlet principal fisica")
        @Valid
        OutletNewDto outlet

) {
}
