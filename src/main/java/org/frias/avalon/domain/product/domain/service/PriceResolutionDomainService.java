package org.frias.avalon.domain.product.domain.service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Domain Service for resolving 3-tier product price and image URL.
 *
 * Tier Hierarchy:
 *   Level 3: Store Local (ProductOutlet)
 *   Level 2: Company Matrix (ProductCompany)
 *   Level 1: Master Global Avalon Catalog
 *
 * Resolution Priority:
 *   Price: localPrice (L3) > customPrice (L2) > basePrice (L1)
 *   Image: localImageUrl (L3) > customImageUrl (L2) > baseImageUrl (L1)
 */
public interface PriceResolutionDomainService {

    /**
     * Resolves effective sale price across 3 tiers.
     *
     * @param localPrice   Optional store-specific price override (Level 3)
     * @param customPrice  Optional company-wide reference price (Level 2)
     * @param basePrice    Global product base price (Level 1)
     * @return Effective price to apply for sale
     */
    BigDecimal resolveEffectivePrice(BigDecimal localPrice, BigDecimal customPrice, BigDecimal basePrice);

    /**
     * Resolves effective display image URL across 3 tiers.
     *
     * @param localImageUrls List of store-specific image URLs (Level 3)
     * @param customImageUrl Company-specific custom image URL (Level 2)
     * @param baseImageUrl   Global product base image URL (Level 1)
     * @return Resolved image URL string
     */
    String resolveEffectiveImageUrl(List<String> localImageUrls, String customImageUrl, String baseImageUrl);
}
