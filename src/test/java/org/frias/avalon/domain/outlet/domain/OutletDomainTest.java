package org.frias.avalon.domain.outlet.domain;

import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for OutletDomain Aggregate Root")
class OutletDomainTest {

    @Test
    @DisplayName("Should create OutletDomain model correctly")
    void shouldCreateOutletDomain() {
        LocationDomain location = new LocationDomain(-12.046374, -77.042793);
        OutletDomain outlet = OutletDomain.create(
                "Tienda Principal",
                "Av. Central 123",
                "987654321",
                "20123456789",
                1L,
                location,
                new BigDecimal("500000"),
                10L
        );

        assertNotNull(outlet);
        assertNotNull(outlet.getCode());
        assertEquals("Tienda Principal", outlet.getName());
        assertEquals("Av. Central 123", outlet.getAddress());
        assertEquals(1L, outlet.getStatusId());
    }
}
