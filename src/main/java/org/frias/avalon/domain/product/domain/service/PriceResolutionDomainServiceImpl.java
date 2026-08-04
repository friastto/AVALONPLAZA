package org.frias.avalon.domain.product.domain.service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Pure Java implementation of PriceResolutionDomainService.
 * Follows Clean Architecture domain service rules (no framework coupling).
 */
public class PriceResolutionDomainServiceImpl implements PriceResolutionDomainService {

    @Override
    public BigDecimal resolveEffectivePrice(BigDecimal localPrice, BigDecimal customPrice, BigDecimal basePrice) {
        // 1. Level 3 Store Override
        if (localPrice != null && localPrice.compareTo(BigDecimal.ZERO) > 0) {
            return localPrice;
        }
        // 2. Level 2 Company Reference
        if (customPrice != null && customPrice.compareTo(BigDecimal.ZERO) > 0) {
            return customPrice;
        }
        // 3. Level 1 Avalon Global Catalog Fallback
        return basePrice != null ? basePrice : BigDecimal.ZERO;
    }

    @Override
    public String resolveEffectiveImageUrl(List<String> localImageUrls, String customImageUrl, String baseImageUrl) {
        // 1. Level 3 Store Local Image
        if (localImageUrls != null && !localImageUrls.isEmpty()) {
            String firstLocal = localImageUrls.get(0);
            if (firstLocal != null && !firstLocal.isBlank()) {
                return firstLocal.trim();
            }
        }
        // 2. Level 2 Company Custom Image
        if (customImageUrl != null && !customImageUrl.isBlank()) {
            return customImageUrl.trim();
        }
        // 3. Level 1 Global Base Image
        return baseImageUrl != null ? baseImageUrl.trim() : "";
    }
}
