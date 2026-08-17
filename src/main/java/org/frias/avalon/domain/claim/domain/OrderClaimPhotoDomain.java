package org.frias.avalon.domain.claim.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderClaimPhotoDomain {
    private Long id;
    private Long claimId;
    private String photoUrl;
    private LocalDateTime createdAt;
}
