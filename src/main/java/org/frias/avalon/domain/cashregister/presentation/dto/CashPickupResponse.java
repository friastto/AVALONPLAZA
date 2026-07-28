package org.frias.avalon.domain.cashregister.presentation.dto;
import lombok.Builder;
import lombok.Data;
import org.frias.avalon.domain.cashregister.domain.CashPickupDomain;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CashPickupResponse {
    private Long id;
    private Long sessionId;
    private Long employeeId;
    private BigDecimal amount;
    private String reason;
    private LocalDateTime pickupTime;

    public static CashPickupResponse fromDomain(CashPickupDomain domain) {
        if (domain == null) return null;
        return CashPickupResponse.builder()
                .id(domain.getId())
                .sessionId(domain.getSessionId())
                .employeeId(domain.getEmployeeId())
                .amount(domain.getAmount())
                .reason(domain.getReason())
                .pickupTime(domain.getPickupTime())
                .build();
    }
}
