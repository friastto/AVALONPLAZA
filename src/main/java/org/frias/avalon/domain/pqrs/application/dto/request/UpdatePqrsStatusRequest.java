package org.frias.avalon.domain.pqrs.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePqrsStatusRequest {

    @NotBlank(message = "El codigo de estado es obligatorio (PEN, PRO, RES, CER)")
    private String statusCode;

    private String adminNotes;
}
