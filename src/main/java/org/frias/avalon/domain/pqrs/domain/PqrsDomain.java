package org.frias.avalon.domain.pqrs.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PqrsDomain {
    private Long id;
    private String ticketNumber;
    private Long userId;
    private Long orderId;
    private Long storeId;
    private String typeCode;
    private String statusCode;
    private String subject;
    private String description;
    private String contactEmail;
    private String contactPhone;
    private String adminNotes;
    private Long respondedByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
