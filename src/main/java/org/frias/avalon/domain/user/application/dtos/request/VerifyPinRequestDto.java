package org.frias.avalon.domain.user.application.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyPinRequestDto(
        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 6, max = 6)
        String pin
) {
}