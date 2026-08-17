package org.frias.avalon.domain.claim.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimItemResponse {
    private Long id;
    private Long orderItemId;
    private Integer quantityAffected;
    private String reason;
}
