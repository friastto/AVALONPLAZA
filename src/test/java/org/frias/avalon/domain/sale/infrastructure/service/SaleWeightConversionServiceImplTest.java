package org.frias.avalon.domain.sale.infrastructure.service;

import org.frias.avalon.core.exeptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SaleWeightConversionServiceImpl Unit Tests")
class SaleWeightConversionServiceImplTest {

    private SaleWeightConversionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SaleWeightConversionServiceImpl();
    }

    // ==========================================
    // Tests para convertToBaseUnit
    // ==========================================

    @Test
    @DisplayName("convertToBaseUnit: Debe lanzar BusinessException cuando la cantidad es nula")
    void convertToBaseUnit_WhenQuantityIsNull_ThrowsBusinessException() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.convertToBaseUnit(null, "KG")
        );
        assertEquals("La cantidad no puede ser nula", exception.getMessage());
    }

    @Test
    @DisplayName("convertToBaseUnit: Debe lanzar BusinessException cuando la unidad de medida es nula")
    void convertToBaseUnit_WhenUnitShortNameIsNull_ThrowsBusinessException() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.convertToBaseUnit(BigDecimal.ONE, null)
        );
        assertEquals("La unidad de medida es requerida", exception.getMessage());
    }

    @Test
    @DisplayName("convertToBaseUnit: Debe convertir LB a Gramos (ej. 2.0 LB -> 907 g y 1.0 lb -> 454 g)")
    void convertToBaseUnit_LB_ConvertsToGramsCorrectly() {
        assertEquals(907, service.convertToBaseUnit(new BigDecimal("2.0"), "LB"));
        assertEquals(454, service.convertToBaseUnit(new BigDecimal("1.0"), " lb "));
        assertEquals(227, service.convertToBaseUnit(new BigDecimal("0.5"), "Lb"));
    }

    @Test
    @DisplayName("convertToBaseUnit: Debe convertir KG a Gramos (ej. 1.5 KG -> 1500 g)")
    void convertToBaseUnit_KG_ConvertsToGramsCorrectly() {
        assertEquals(1500, service.convertToBaseUnit(new BigDecimal("1.5"), "KG"));
        assertEquals(2350, service.convertToBaseUnit(new BigDecimal("2.35"), " kg "));
        assertEquals(1000, service.convertToBaseUnit(new BigDecimal("1.00"), "Kg"));
    }

    @Test
    @DisplayName("convertToBaseUnit: Debe mantener Gramos (G) sin multiplicar")
    void convertToBaseUnit_G_ReturnsSameQuantity() {
        assertEquals(251, service.convertToBaseUnit(new BigDecimal("250.75"), "G"));
        assertEquals(100, service.convertToBaseUnit(new BigDecimal("100.20"), " g "));
        assertEquals(50, service.convertToBaseUnit(new BigDecimal("50"), "g"));
    }

    @Test
    @DisplayName("convertToBaseUnit: Debe mantener Mililitros (ML) sin multiplicar")
    void convertToBaseUnit_ML_ReturnsSameQuantity() {
        assertEquals(500, service.convertToBaseUnit(new BigDecimal("500.49"), "ML"));
        assertEquals(751, service.convertToBaseUnit(new BigDecimal("750.51"), " ml "));
        assertEquals(250, service.convertToBaseUnit(new BigDecimal("250"), "mL"));
    }

    @Test
    @DisplayName("convertToBaseUnit: Debe convertir L (Litros) a Mililitros")
    void convertToBaseUnit_L_ConvertsToMillilitersCorrectly() {
        assertEquals(500, service.convertToBaseUnit(new BigDecimal("0.5"), "L"));
        assertEquals(1250, service.convertToBaseUnit(new BigDecimal("1.25"), " l "));
        assertEquals(2000, service.convertToBaseUnit(new BigDecimal("2.0"), "L"));
    }

    @Test
    @DisplayName("convertToBaseUnit: Debe lanzar BusinessException para unidades no soportadas (ej. UND, GR, PCS)")
    void convertToBaseUnit_UnsupportedUnits_ThrowsBusinessException() {
        BusinessException exUnd = assertThrows(
                BusinessException.class,
                () -> service.convertToBaseUnit(BigDecimal.TEN, "UND")
        );
        assertTrue(exUnd.getMessage().contains("Unidad de medida 'UND' no soportada"));

        BusinessException exGr = assertThrows(
                BusinessException.class,
                () -> service.convertToBaseUnit(BigDecimal.TEN, "GR")
        );
        assertTrue(exGr.getMessage().contains("Unidad de medida 'GR' no soportada"));

        BusinessException exEmpty = assertThrows(
                BusinessException.class,
                () -> service.convertToBaseUnit(BigDecimal.ONE, "")
        );
        assertTrue(exEmpty.getMessage().contains("no soportada"));
    }

    // ==========================================
    // Tests para formatFromBaseUnit
    // ==========================================

    @Test
    @DisplayName("formatFromBaseUnit: Debe retornar '0' cuando la cantidad base es nula")
    void formatFromBaseUnit_WhenBaseQuantityIsNull_ReturnsZero() {
        assertEquals("0", service.formatFromBaseUnit(null, "KG"));
    }

    @Test
    @DisplayName("formatFromBaseUnit: Debe retornar la cantidad base como String cuando la unidad es nula")
    void formatFromBaseUnit_WhenUnitShortNameIsNull_ReturnsBaseQuantityAsString() {
        assertEquals("150", service.formatFromBaseUnit(150, null));
    }

    @Test
    @DisplayName("formatFromBaseUnit: Debe formatear a LB correctamente desde gramos")
    void formatFromBaseUnit_LB_FormatsCorrectly() {
        assertEquals("2 LB", service.formatFromBaseUnit(907, "LB"));
        assertEquals("1 lb", service.formatFromBaseUnit(454, "lb"));
    }

    @Test
    @DisplayName("formatFromBaseUnit: Debe formatear a KG correctamente desde gramos")
    void formatFromBaseUnit_KG_FormatsCorrectly() {
        assertEquals("1.5 KG", service.formatFromBaseUnit(1500, "KG"));
        assertEquals("1 kg", service.formatFromBaseUnit(1000, "kg"));
    }

    @Test
    @DisplayName("formatFromBaseUnit: Debe formatear a G (Gramos) correctamente")
    void formatFromBaseUnit_G_FormatsCorrectly() {
        assertEquals("250 G", service.formatFromBaseUnit(250, "G"));
        assertEquals("500 g", service.formatFromBaseUnit(500, "g"));
    }

    @Test
    @DisplayName("formatFromBaseUnit: Debe formatear a ML (Mililitros) correctamente")
    void formatFromBaseUnit_ML_FormatsCorrectly() {
        assertEquals("750 ML", service.formatFromBaseUnit(750, "ML"));
        assertEquals("100 ml", service.formatFromBaseUnit(100, "ml"));
    }

    @Test
    @DisplayName("formatFromBaseUnit: Debe formatear a L (Litros) correctamente")
    void formatFromBaseUnit_L_FormatsCorrectly() {
        assertEquals("0.5 L", service.formatFromBaseUnit(500, "L"));
        assertEquals("1.25 l", service.formatFromBaseUnit(1250, "l"));
    }

    @Test
    @DisplayName("formatFromBaseUnit: Debe retornar cantidad y unidad original para unidades no soportadas")
    void formatFromBaseUnit_UnsupportedUnits_ReturnsBaseQuantityWithUnitName() {
        assertEquals("10 UND", service.formatFromBaseUnit(10, "UND"));
        assertEquals("5 BOX", service.formatFromBaseUnit(5, "BOX"));
        assertEquals("12 GR", service.formatFromBaseUnit(12, "GR"));
    }

    // ==========================================
    // Tests para isWeighable
    // ==========================================

    @Test
    @DisplayName("isWeighable: Debe retornar false cuando la unidad es nula")
    void isWeighable_WhenUnitIsNull_ReturnsFalse() {
        assertFalse(service.isWeighable(null));
    }

    @Test
    @DisplayName("isWeighable: Debe retornar true para unidades pesables/volumen soportadas (KG, G, LB, L, ML)")
    void isWeighable_SupportedUnits_ReturnsTrue() {
        assertTrue(service.isWeighable("KG"));
        assertTrue(service.isWeighable("G"));
        assertTrue(service.isWeighable("LB"));
        assertTrue(service.isWeighable("L"));
        assertTrue(service.isWeighable("ML"));
        assertTrue(service.isWeighable(" kg "));
        assertTrue(service.isWeighable(" g "));
        assertTrue(service.isWeighable(" lb "));
        assertTrue(service.isWeighable(" l "));
        assertTrue(service.isWeighable(" ml "));
    }

    @Test
    @DisplayName("isWeighable: Debe retornar false para unidades no pesables o no reconocidas")
    void isWeighable_UnsupportedUnits_ReturnsFalse() {
        assertFalse(service.isWeighable("UND"));
        assertFalse(service.isWeighable("PCS"));
        assertFalse(service.isWeighable("BOX"));
        assertFalse(service.isWeighable("GR"));
        assertFalse(service.isWeighable(""));
        assertFalse(service.isWeighable("   "));
    }
}
