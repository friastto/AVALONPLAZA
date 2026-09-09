package org.frias.avalon.domain.company.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssignCompanyManagerRequest(
        Long personId,
        Long userId,
        @NotNull(message = "El tipo de documento es obligatorio")
        Long identificationTypeId,
        @NotBlank(message = "El numero de identificacion es obligatorio")
        String identificationNumber,
        @NotBlank(message = "El primer nombre es obligatorio")
        String firstName,
        @NotBlank(message = "El primer apellido es obligatorio")
        String firstLastName,
        String address,
        Long sexId,
        String email,
        String phone,
        String username,
        String password
) {}
