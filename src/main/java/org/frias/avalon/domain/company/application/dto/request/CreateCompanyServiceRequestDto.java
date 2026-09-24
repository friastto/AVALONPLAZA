package org.frias.avalon.domain.company.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCompanyServiceRequestDto(
        @NotBlank(message = "El nombre de la empresa es obligatorio")
        @Size(max = 255, message = "El nombre no debe exceder 255 caracteres")
        String companyName,

        @NotBlank(message = "El NIT de la empresa es obligatorio")
        @Size(max = 20, message = "El NIT no debe exceder 20 caracteres")
        String companyNit,

        String companyEmail,

        @NotBlank(message = "El nombre de la tienda inicial es obligatorio")
        @Size(max = 255, message = "El nombre de la tienda no debe exceder 255 caracteres")
        String storeName,

        @NotBlank(message = "La direccion de la tienda es obligatoria")
        String storeAddress,

        String storePhone,

        Double latitude,

        Double longitude,

        @NotNull(message = "El ID del usuario solicitante es obligatorio")
        Long applicantUserId,

        String verificationToken
) {
}
