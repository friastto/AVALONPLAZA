package org.frias.avalon.domain.cashregister.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdConfigurationResponse {
    private BigDecimal warningThreshold;
    private BigDecimal blockThreshold;
}
