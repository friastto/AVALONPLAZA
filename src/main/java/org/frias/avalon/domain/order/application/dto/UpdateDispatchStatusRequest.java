package org.frias.avalon.domain.order.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDispatchStatusRequest {
    @NotNull(message = "El statusId es obligatorio")
    private Long statusId;
}
