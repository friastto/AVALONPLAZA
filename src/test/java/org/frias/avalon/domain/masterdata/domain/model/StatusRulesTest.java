package org.frias.avalon.domain.masterdata.domain.model;

import org.frias.avalon.core.exeptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class StatusRulesTest {

    private final MasterRoot ACT = new MasterRoot(1L, "ACT", "Activo", null, 1L);
    private final MasterRoot INA = new MasterRoot(2L, "INA", "Inactivo", null, 1L);
    private final MasterRoot SUS = new MasterRoot(3L, "SUS", "Suspendido", null, 1L);
    private final MasterRoot DEL = new MasterRoot(4L, "DEL", "Eliminado", null, 1L);
    private final MasterRoot UNKNOWN = new MasterRoot(5L, "UNK", "Desconocido", null, 1L);


    @Test
    @DisplayName("should allow valid transition from ACT to INA")
    void validateTransition_shouldAllowValidTransitionFromActToIna() {
        assertDoesNotThrow(() -> StatusRules.validateTransition(ACT, INA));
    }

    @Test
    @DisplayName("should allow valid transition from ACT to SUS")
    void validateTransition_shouldAllowValidTransitionFromActToSus() {
        assertDoesNotThrow(() -> StatusRules.validateTransition(ACT, SUS));
    }

    @Test
    @DisplayName("should allow valid transition from ACT to DEL")
    void validateTransition_shouldAllowValidTransitionFromActToDel() {
        assertDoesNotThrow(() -> StatusRules.validateTransition(ACT, DEL));
    }

    @Test
    @DisplayName("should allow valid transition from INA to ACT")
    void validateTransition_shouldAllowValidTransitionFromInaToAct() {
        assertDoesNotThrow(() -> StatusRules.validateTransition(INA, ACT));
    }

    @Test
    @DisplayName("should allow valid transition from INA to DEL")
    void validateTransition_shouldAllowValidTransitionFromInaToDel() {
        assertDoesNotThrow(() -> StatusRules.validateTransition(INA, DEL));
    }

    @Test
    @DisplayName("should allow valid transition from SUS to ACT")
    void validateTransition_shouldAllowValidTransitionFromSusToAct() {
        assertDoesNotThrow(() -> StatusRules.validateTransition(SUS, ACT));
    }

    @Test
    @DisplayName("should allow valid transition from SUS to DEL")
    void validateTransition_shouldAllowValidTransitionFromSusToDel() {
        assertDoesNotThrow(() -> StatusRules.validateTransition(SUS, DEL));
    }

    @Test
    @DisplayName("should throw DomainValidationException if current and next status are the same")
    void validateTransition_shouldThrowExceptionIfCurrentAndNextAreSame() {
        assertThatThrownBy(() -> StatusRules.validateTransition(ACT, ACT))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Ya tiene ese estado");
    }

    @Test
    @DisplayName("should throw DomainValidationException for invalid transition from ACT to ACT (same status)")
    void validateTransition_shouldThrowExceptionForInvalidTransitionActToAct() {
        assertThatThrownBy(() -> StatusRules.validateTransition(ACT, ACT))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Ya tiene ese estado");
    }

    @Test
    @DisplayName("should throw DomainValidationException for invalid transition from INA to SUS")
    void validateTransition_shouldThrowExceptionForInvalidTransitionInaToSus() {
        assertThatThrownBy(() -> StatusRules.validateTransition(INA, SUS))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Transición no permitida");
    }

    @Test
    @DisplayName("should throw DomainValidationException for invalid transition from DEL to ACT")
    void validateTransition_shouldThrowExceptionForInvalidTransitionDelToAct() {
        assertThatThrownBy(() -> StatusRules.validateTransition(DEL, ACT))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Transición no permitida");
    }

    @Test
    @DisplayName("should throw DomainValidationException if current status is not defined in transitions")
    void validateTransition_shouldThrowExceptionIfCurrentStatusNotDefined() {
        assertThatThrownBy(() -> StatusRules.validateTransition(UNKNOWN, ACT))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Estado actual inválido: MasterRoot(id=5, shortName=UNK, fullName=Desconocido, parentId=null, statusId=1)");
    }
}
