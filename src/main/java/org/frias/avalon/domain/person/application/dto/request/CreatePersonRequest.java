package org.frias.avalon.domain.person.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreatePersonRequest(
        Long typeIdentificationId,
        String typeIdentificationCode,
        @NotBlank(message = "El numero de identificacion es requerido")
        String numberid,
        @NotBlank(message = "El nombre es requerido")
        String name,
        @NotBlank(message = "El apellido es requerido")
        String lastName,
        @NotBlank(message = "La direccion de residencia es requerida")
        String address,
        Long sexId,
        String sexCode,
        Long phoneNumber,
        @Email(message = "El formato del email no es valido")
        String email,
        Long statusId,
        String statusCode
) {
    public CreatePersonRequest(
            Long typeIdentificationId,
            String numberid,
            String name,
            String lastName,
            String address,
            Long sexId,
            Long phoneNumber,
            String email,
            Long statusId
    ) {
        this(typeIdentificationId, null, numberid, name, lastName, address, sexId, null, phoneNumber, email, statusId, null);
    }
}