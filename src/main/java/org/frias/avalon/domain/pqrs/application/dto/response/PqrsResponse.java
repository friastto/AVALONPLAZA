package org.frias.avalon.domain.pqrs.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PqrsResponse {
    private Long id;
    private String ticketNumber;
    private Long userId;
    private String userName;
    private Long orderId;
    private Long storeId;
    private String typeCode;
    private String typeName;
    private MasterRefDto type;
    private String statusCode;
    private String statusName;
    private MasterRefDto status;
    private String subject;
    private String description;
    private String contactEmail;
    private String contactPhone;
    private String adminNotes;
    private Long respondedByUserId;
    private String respondedByUserName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
