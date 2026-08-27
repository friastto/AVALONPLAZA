package org.frias.avalon.domain.product.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for PriceResolutionDomainServiceImpl 3-Level Resolution")
class PriceResolutionDomainServiceImplTest {

    private PriceResolutionDomainServiceImpl priceResolutionService;

    @BeforeEach
    void setUp() {
        priceResolutionService = new PriceResolutionDomainServiceImpl();
    }

    @Test
    @DisplayName("Should resolve level 3 local store price when present")
    void shouldResolveLevel3LocalPrice() {
        BigDecimal localPrice = new BigDecimal("1500.00");
        BigDecimal customPrice = new BigDecimal("1800.00");
        BigDecimal basePrice = new BigDecimal("2000.00");

        BigDecimal result = priceResolutionService.resolveEffectivePrice(localPrice, customPrice, basePrice);
        assertEquals(new BigDecimal("1500.00"), result);
    }

    @Test
    @DisplayName("Should resolve level 2 company price when level 3 is null or zero")
    void shouldResolveLevel2CompanyPrice() {
        BigDecimal customPrice = new BigDecimal("1800.00");
        BigDecimal basePrice = new BigDecimal("2000.00");

        BigDecimal result1 = priceResolutionService.resolveEffectivePrice(null, customPrice, basePrice);
        assertEquals(new BigDecimal("1800.00"), result1);

        BigDecimal result2 = priceResolutionService.resolveEffectivePrice(BigDecimal.ZERO, customPrice, basePrice);
        assertEquals(new BigDecimal("1800.00"), result2);
    }

    @Test
    @DisplayName("Should resolve level 1 global base price when level 3 and level 2 are absent")
    void shouldResolveLevel1GlobalPrice() {
        BigDecimal basePrice = new BigDecimal("2000.00");

        BigDecimal result = priceResolutionService.resolveEffectivePrice(null, null, basePrice);
        assertEquals(new BigDecimal("2000.00"), result);
    }

    @Test
    @DisplayName("Should return ZERO if all 3 price levels are null")
    void shouldReturnZeroWhenAllPricesNull() {
        BigDecimal result = priceResolutionService.resolveEffectivePrice(null, null, null);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("Should resolve effective image URL following 3-level priority")
    void shouldResolveEffectiveImageUrl() {
        String level3 = priceResolutionService.resolveEffectiveImageUrl(List.of("https://cdn.com/store.jpg"), "https://cdn.com/company.jpg", "https://cdn.com/global.jpg");
        assertEquals("https://cdn.com/store.jpg", level3);

        String level2 = priceResolutionService.resolveEffectiveImageUrl(Collections.emptyList(), "https://cdn.com/company.jpg", "https://cdn.com/global.jpg");
        assertEquals("https://cdn.com/company.jpg", level2);

        String level1 = priceResolutionService.resolveEffectiveImageUrl(null, "", "https://cdn.com/global.jpg");
        assertEquals("https://cdn.com/global.jpg", level1);

        String fallback = priceResolutionService.resolveEffectiveImageUrl(null, null, null);
        assertEquals("", fallback);
    }
}
