package org.frias.avalon.domain.claim.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimItemRequest {
    @NotNull(message = "El orderItemId es obligatorio")
    private Long orderItemId;

    private Integer quantityAffected;

    @DecimalMin(value = "0.001", message = "La cantidad afectada debe ser mayor a 0")
    private BigDecimal decimalQuantityAffected;

    private String reason;

    public Integer getEffectiveQuantityAffected() {
        if (decimalQuantityAffected != null) {
            return decimalQuantityAffected.intValue();
        }
        return quantityAffected != null ? quantityAffected : 1;
    }
}
