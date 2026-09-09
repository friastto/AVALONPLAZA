package org.frias.avalon.domain.company.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssignCompanyEmployeeRequest(
        @NotNull(message = "El tipo de identificacion es obligatorio")
        Long typeIdentificationId,

        @NotBlank(message = "El numero de identificacion es obligatorio")
        String numberid,

        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @NotBlank(message = "El apellido es obligatorio")
        String lastName,

        String email,
        Long phoneNumber,
        String address,
        Long sexId,

        String userName,
        String password,

        @NotNull(message = "El rol es obligatorio")
        Long roleId,

        Long outletId
) {
}
