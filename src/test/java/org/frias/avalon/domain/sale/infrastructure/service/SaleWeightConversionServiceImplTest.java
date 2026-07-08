package org.frias.avalon.domain.sale.infrastructure.service;

import org.frias.avalon.core.exeptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SaleWeightConversionServiceImplTest {

    private SaleWeightConversionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SaleWeightConversionServiceImpl();
    }

    @Test
    void testIsWeighable() {
        assertTrue(service.isWeighable("KG"));
        assertTrue(service.isWeighable("G"));
        assertTrue(service.isWeighable("LB"));
        assertTrue(service.isWeighable("L"));
        assertTrue(service.isWeighable("ML"));
        assertFalse(service.isWeighable("UND"));
        assertFalse(service.isWeighable(null));
    }

    @Test
    void testConvertToBaseUnitKG() {
        Integer grams = service.convertToBaseUnit(new BigDecimal("1.5"), "KG");
        assertEquals(1500, grams);
    }

    @Test
    void testConvertToBaseUnitLB() {
        Integer grams = service.convertToBaseUnit(new BigDecimal("2.0"), "LB");
        // 2 * 453.59237 = 907.18474 -> Redondeado a 907
        assertEquals(907, grams);
    }

    @Test
    void testConvertToBaseUnitL() {
        Integer ml = service.convertToBaseUnit(new BigDecimal("0.5"), "L");
        assertEquals(500, ml);
    }

    @Test
    void testConvertToBaseUnitUnsupported() {
        assertThrows(BusinessException.class, () -> service.convertToBaseUnit(BigDecimal.ONE, "XYZ"));
    }

    @Test
    void testFormatFromBaseUnitKG() {
        String formatted = service.formatFromBaseUnit(1500, "KG");
        assertEquals("1.5 KG", formatted);
    }

    @Test
    void testFormatFromBaseUnitL() {
        String formatted = service.formatFromBaseUnit(500, "L");
        assertEquals("0.5 L", formatted);
    }

    @Test
    void testFormatFromBaseUnitUnsupported() {
        String formatted = service.formatFromBaseUnit(10, "UND");
        assertEquals("10 UND", formatted);
    }
}
