package org.frias.avalon.domain.cashregister.application.dto;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class BlindCountStep2Request {
    private Long managerId;
    private BigDecimal managerCountedCash;
    private String justification;
}
