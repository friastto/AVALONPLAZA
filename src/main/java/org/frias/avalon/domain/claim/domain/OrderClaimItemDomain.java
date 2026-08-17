package org.frias.avalon.domain.claim.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderClaimItemDomain {
    private Long id;
    private Long claimId;
    private Long orderItemId;
    private Integer quantityAffected;
    private String reason;
}
