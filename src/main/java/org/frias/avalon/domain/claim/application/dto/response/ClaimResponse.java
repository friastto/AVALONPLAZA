package org.frias.avalon.domain.claim.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {
    private Long id;
    private Long orderId;
    private Long customerId;
    private Long claimTypeId;
    private Long statusId;
    private String description;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ClaimItemResponse> items;
    private List<String> photoUrls;
}
