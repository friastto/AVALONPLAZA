package org.frias.avalon.domain.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDispatchStatusRequest {
    private Long statusId;
    private String statusCode;
}
