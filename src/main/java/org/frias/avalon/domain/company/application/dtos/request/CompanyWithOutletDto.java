package org.frias.avalon.domain.company.application.dtos.request;


import jakarta.validation.constraints.NotBlank;
import org.frias.avalon.domain.outlet.dtos.response.OutletDto;

public record CompanyWithOutletDto(

        @NotBlank(message = "La empresa debe tener un nit")
    String nit,

        @NotBlank(message = "La empresa no piuede estar sin name")
    String name,

        @NotBlank(message = "La empresa debe tener un email de contacto")
    String email,

        @NotBlank(message = "La empresa debe tener una outlet principal fisica")
    OutletDto outlet

){}
