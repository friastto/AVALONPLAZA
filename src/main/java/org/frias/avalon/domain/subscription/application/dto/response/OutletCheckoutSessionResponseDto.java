package org.frias.avalon.domain.subscription.application.dto.response;

import java.math.BigDecimal;

/**
 * Output DTO providing the parameters and SHA-256 signature to initialize a Wompi checkout session.
 */
public record OutletCheckoutSessionResponseDto(
        String reference,
        String wompiReference,
        Long amountInCents,
        BigDecimal amountCop,
        String currency,
        String publicKey,
        String integritySignature,
        String checkoutUrl,
        Long outletId,
        String outletName,
        Long companyId,
        String customerEmail
) {
}
