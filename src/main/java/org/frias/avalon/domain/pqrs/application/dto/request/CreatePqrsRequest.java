package org.frias.avalon.domain.pqrs.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePqrsRequest {

    @NotBlank(message = "El tipo de PQRS es obligatorio (PETICION, QUEJA, RECLAMO, SUGERENCIA)")
    private String typeCode;

    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 200, message = "El asunto no puede superar los 200 caracteres")
    private String subject;

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(min = 10, message = "La descripcion debe contener al menos 10 caracteres")
    private String description;

    private Long orderId;
    private Long storeId;
    private String contactEmail;
    private String contactPhone;
}
