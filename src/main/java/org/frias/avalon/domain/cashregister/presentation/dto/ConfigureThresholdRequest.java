package org.frias.avalon.domain.cashregister.presentation.dto;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class ConfigureThresholdRequest {
    private BigDecimal thresholdAmount;
}
