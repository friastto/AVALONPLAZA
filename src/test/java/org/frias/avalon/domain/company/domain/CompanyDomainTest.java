package org.frias.avalon.domain.company.domain;

import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for CompanyDomain Record")
class CompanyDomainTest {

    @Test
    @DisplayName("Should create CompanyDomain model correctly")
    void shouldCreateCompanyDomain() {
        CompanyDomain company = new CompanyDomain(
                1L,
                "20123456789",
                "Empresa Demo",
                "contacto@empresa.com",
                1L,
                new BigDecimal("500000"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        assertNotNull(company);
        assertEquals(1L, company.id());
        assertEquals("Empresa Demo", company.name());
        assertEquals("20123456789", company.nit());
        assertEquals("contacto@empresa.com", company.email());
        assertEquals(1L, company.statusId());
    }

    @Test
    @DisplayName("Should throw exception when nit is blank")
    void shouldThrowExceptionWhenNitIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                new CompanyDomain(1L, "   ", "Name", "e@mail.com", 1L, BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now()));
    }
}
