package org.frias.avalon.domain.person.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyIdentificationRequestDto(
        @NotBlank(message = "El número de identificación no puede estar vacío")
        String identificationNumber
) {
}