package org.frias.avalon.domain.claim.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimItemRequest {
    @NotNull(message = "El orderItemId es obligatorio")
    private Long orderItemId;

    @NotNull(message = "La cantidad afectada es obligatoria")
    @Min(value = 1, message = "La cantidad afectada debe ser al menos 1")
    private Integer quantityAffected;

    private String reason;
}
