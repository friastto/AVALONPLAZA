package org.frias.avalon.domain.company.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for SuperAdmin rejecting a Company Service Request.
 */
public record RejectCompanyServiceRequestDto(
        @NotBlank(message = "El motivo de rechazo es obligatorio")
        String reasonCategory,

        @NotBlank(message = "La explicacion del rechazo es obligatoria")
        String explanationMessage
) {
}
