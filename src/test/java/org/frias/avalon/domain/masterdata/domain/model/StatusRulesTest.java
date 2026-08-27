package org.frias.avalon.domain.masterdata.domain.model;

import org.frias.avalon.core.exeptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias integrales para las reglas de transición de estado en StatusRules.
 */
@DisplayName("Pruebas Unitarias para StatusRules - Reglas de Transición de Estado")
class StatusRulesTest {

    @Nested
    @DisplayName("Transiciones Válidas")
    class ValidTransitionsTests {

        @ParameterizedTest(name = "Debería permitir la transición válida de {0} a {1}")
        @CsvSource({
                "ACT, INA",
                "ACT, SUS",
                "ACT, DEL",
                "INA, ACT",
                "INA, DEL",
                "SUS, ACT",
                "SUS, DEL"
        })
        void shouldAllowValidTransition(String currentCode, String nextCode) {
            // Arrange
            MasterRoot current = new MasterRoot(1L, currentCode, "Current Status", null, 100L);
            MasterRoot next = new MasterRoot(2L, nextCode, "Next Status", null, 100L);

            // Act & Assert
            assertDoesNotThrow(() -> StatusRules.validateTransition(current, next));
        }
    }

    @Nested
    @DisplayName("Transiciones Inválidas por Mismo Estado")
    class SameStatusTransitionsTests {

        @ParameterizedTest(name = "Debería lanzar DomainValidationException al intentar cambiar de {0} a {0}")
        @CsvSource({
                "ACT",
                "INA",
                "SUS",
                "DEL"
        })
        void shouldThrowExceptionWhenTransitioningToSameStatus(String code) {
            // Arrange
            MasterRoot current = new MasterRoot(1L, code, "Status", null, 100L);
            MasterRoot next = new MasterRoot(1L, code, "Status", null, 100L);

            // Act & Assert
            DomainValidationException ex = assertThrows(
                    DomainValidationException.class,
                    () -> StatusRules.validateTransition(current, next)
            );
            assertEquals("Ya tiene ese estado", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Transiciones Inválidas por Reglas de Dominio")
    class DisallowedTransitionsTests {

        @ParameterizedTest(name = "Debería rechazar la transición no permitida de {0} a {1}")
        @CsvSource({
                "INA, SUS",
                "SUS, INA",
                "DEL, ACT",
                "DEL, INA",
                "DEL, SUS"
        })
        void shouldThrowExceptionForDisallowedTransition(String currentCode, String nextCode) {
            // Arrange
            MasterRoot current = new MasterRoot(1L, currentCode, "Current Status", null, 100L);
            MasterRoot next = new MasterRoot(2L, nextCode, "Next Status", null, 100L);

            // Act & Assert
            DomainValidationException ex = assertThrows(
                    DomainValidationException.class,
                    () -> StatusRules.validateTransition(current, next)
            );
            assertTrue(ex.getMessage().startsWith("Transición no permitida:"));
        }
    }

    @Nested
    @DisplayName("Estado Actual Desconocido o Inválido")
    class UnknownStatusTests {

        @Test
        @DisplayName("Debería lanzar DomainValidationException si el estado actual no está configurado")
        void shouldThrowExceptionForUnknownCurrentStatus() {
            // Arrange
            MasterRoot current = new MasterRoot(1L, "UNKNOWN", "Desconocido", null, 100L);
            MasterRoot next = new MasterRoot(2L, "ACT", "Activo", null, 100L);

            // Act & Assert
            DomainValidationException ex = assertThrows(
                    DomainValidationException.class,
                    () -> StatusRules.validateTransition(current, next)
            );
            assertTrue(ex.getMessage().startsWith("Estado actual inválido:"));
        }
    }
}
