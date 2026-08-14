package org.frias.avalon.domain.cashregister.application.dto;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class BlindCountStep1Request {
    private Long employeeId;
    private BigDecimal actualCash;
}
