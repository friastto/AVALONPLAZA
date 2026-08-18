package org.frias.avalon.domain.outlet.application.dto.request;

import java.math.BigDecimal;

public record UpdateDeliverySettingsRequestDto(
        Boolean deliveryEnabled,
        BigDecimal deliveryFee
) {
}
