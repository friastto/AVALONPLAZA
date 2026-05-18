package org.frias.avalon.domain.person.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePersonRequest(
        @NotNull(message = "El tipo de identificación es requerido")
        Long typeIdentificationId,
        @NotBlank(message = "El número de identificación es requerido")
        String numberid,
        @NotBlank(message = "El nombre es requerido")
        String name,
        @NotBlank(message = "El apellido es requerido")
        String lastName,
        @NotBlank(message = "la direccion de residencia es requerida")
        String address,
        Long sexId,
        Long phoneNumber,
        @Email(message = "El formato del email no es válido")
        String email,
        @NotNull(message = "El estado es requerido")
        Long statusId
) {
}