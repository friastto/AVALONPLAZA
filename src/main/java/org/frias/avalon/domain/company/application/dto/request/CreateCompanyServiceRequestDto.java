package org.frias.avalon.domain.company.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCompanyServiceRequestDto(
        String requestType, // "PERSONA_NATURAL" or "EMPRESA" (defaults to "EMPRESA")

        @Size(max = 255, message = "El nombre de la empresa no debe exceder 255 caracteres")
        String companyName,

        @Size(max = 20, message = "El NIT no debe exceder 20 caracteres")
        String companyNit,

        String companyEmail,

        @NotBlank(message = "El nombre de la tienda inicial es obligatorio")
        @Size(max = 255, message = "El nombre de la tienda no debe exceder 255 caracteres")
        String storeName,

        @Size(max = 20, message = "El NIT de la tienda no debe exceder 20 caracteres")
        String storeNit,

        @NotBlank(message = "La direccion de la tienda es obligatoria")
        String storeAddress,

        String storePhone,

        Double latitude,

        Double longitude,

        @NotNull(message = "El ID del usuario solicitante es obligatorio")
        Long applicantUserId,

        String verificationToken
) {
    // Backward-compatible constructor for previous 10-parameter calls
    public CreateCompanyServiceRequestDto(
            String companyName,
            String companyNit,
            String companyEmail,
            String storeName,
            String storeAddress,
            String storePhone,
            Double latitude,
            Double longitude,
            Long applicantUserId,
            String verificationToken
    ) {
        this(
                "EMPRESA",
                companyName,
                companyNit,
                companyEmail,
                storeName,
                companyNit,
                storeAddress,
                storePhone,
                latitude,
                longitude,
                applicantUserId,
                verificationToken
        );
    }
}
