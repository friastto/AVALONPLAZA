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
public class ThreeStepAuditRequest {
    private BigDecimal baseCash;
    private BigDecimal remainingCash;
    private BigDecimal cashDropsTotal;
    private String notes;
}
