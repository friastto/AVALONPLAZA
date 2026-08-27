package org.frias.avalon.domain.product.domain.service;

import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for UnitConversionServiceImpl Unit Conversion Domain")
class UnitConversionServiceImplTest {

    private MasterTreeProvider masterTreeProvider;
    private MasterTree masterTree;
    private UnitConversionServiceImpl unitConversionService;

    @BeforeEach
    void setUp() {
        masterTreeProvider = mock(MasterTreeProvider.class);
        masterTree = mock(MasterTree.class);
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        unitConversionService = new UnitConversionServiceImpl(masterTreeProvider);
    }

    @Test
    @DisplayName("Should convert KG, LB, UND, L correctly to smallest base unit")
    void shouldConvertToSmallestUnitSuccessfully() {
        assertEquals(1500, unitConversionService.convertToSmallestUnit(new BigDecimal("1.5"), "KG"));
        assertEquals(1000, unitConversionService.convertToSmallestUnit(new BigDecimal("1"), "L"));
        assertEquals(5, unitConversionService.convertToSmallestUnit(new BigDecimal("5"), "UND"));
        assertEquals(500, unitConversionService.convertToSmallestUnit(new BigDecimal("500"), "G"));
        assertEquals(0, unitConversionService.convertToSmallestUnit(null, "KG"));
    }

    @Test
    @DisplayName("Should throw DomainValidationException for invalid or unknown units")
    void shouldThrowExceptionForInvalidUnits() {
        assertThrows(DomainValidationException.class, () -> unitConversionService.convertToSmallestUnit(new BigDecimal("1"), ""));
        assertThrows(DomainValidationException.class, () -> unitConversionService.convertToSmallestUnit(new BigDecimal("1"), "UNRECOGNIZED_UNIT"));
    }

    @Test
    @DisplayName("Should convert from smallest base unit to display string")
    void shouldConvertFromSmallestUnitSuccessfully() {
        MasterRoot kgNode = new MasterRoot(10L, "KG", "Kilogramo", 0L, 1L);
        MasterRoot undNode = new MasterRoot(11L, "UND", "Unidad", 0L, 1L);

        when(masterTree.getByIdOrThrow(10L)).thenReturn(kgNode);
        when(masterTree.getByIdOrThrow(11L)).thenReturn(undNode);

        assertEquals("1.5 KG", unitConversionService.convertFromSmallestUnit(1500, 10L));
        assertEquals("5 UND", unitConversionService.convertFromSmallestUnit(5, 11L));
        assertEquals("0", unitConversionService.convertFromSmallestUnit(null, 10L));
    }
}
