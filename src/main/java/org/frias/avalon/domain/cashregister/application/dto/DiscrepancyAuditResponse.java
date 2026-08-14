package org.frias.avalon.domain.cashregister.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscrepancyAuditResponse {
    private Long id;
    private Long cashSessionId;
    private BigDecimal expectedCash;
    private BigDecimal actualCash;
    private BigDecimal difference;
    private Long auditedBy;
    private String createdAt;
}
