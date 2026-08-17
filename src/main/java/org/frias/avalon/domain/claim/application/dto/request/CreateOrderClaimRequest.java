package org.frias.avalon.domain.claim.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderClaimRequest {
    @NotNull(message = "El orderId es obligatorio")
    private Long orderId;

    private Long customerId;

    @NotNull(message = "El claimTypeId es obligatorio")
    private Long claimTypeId;

    @NotBlank(message = "La descripcion del reclamo es obligatoria")
    private String description;

    @NotEmpty(message = "El reclamo debe incluir al menos un producto afectado")
    private List<ClaimItemRequest> items;

    @NotEmpty(message = "El reclamo requiere al menos una fotografia adjunta")
    private List<String> photoUrls;
}
