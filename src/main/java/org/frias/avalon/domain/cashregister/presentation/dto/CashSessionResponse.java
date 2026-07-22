package org.frias.avalon.domain.cashregister.presentation.dto;

import lombok.Builder;
import lombok.Data;
import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CashSessionResponse {

    private Long id;
    private Long outletId;
    private Long employeeId;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private BigDecimal initialBase;
    private BigDecimal expectedCash;
    private BigDecimal actualCash;
    private BigDecimal difference;
    private String status;
    private String notes;

    public static CashSessionResponse fromDomain(CashSessionDomain domain) {
        if (domain == null) return null;
        return CashSessionResponse.builder()
                .id(domain.getId())
                .outletId(domain.getOutletId())
                .employeeId(domain.getEmployeeId())
                .openedAt(domain.getOpenedAt())
                .closedAt(domain.getClosedAt())
                .initialBase(domain.getInitialBase())
                .expectedCash(domain.getExpectedCash())
                .actualCash(domain.getActualCash())
                .difference(domain.getDifference())
                .status(domain.getStatus())
                .notes(domain.getNotes())
                .build();
    }
}
