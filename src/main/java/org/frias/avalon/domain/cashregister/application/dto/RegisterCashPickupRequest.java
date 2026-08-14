package org.frias.avalon.domain.cashregister.application.dto;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class RegisterCashPickupRequest {
    private BigDecimal amount;
    private String reason;
    private Long registeredBy;
}
