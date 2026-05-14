package org.frias.avalon.domain.masterdata.domain.model;

import org.frias.avalon.core.exeptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MasterRootTest {

    private final Long SAMPLE_ID = 1L;
    private final String SAMPLE_SHORT_NAME = "TEST";
    private final String SAMPLE_FULL_NAME = "Test Master Data";
    private final Long SAMPLE_PARENT_ID = 10L;
    private final Long SAMPLE_STATUS_ID = 100L; // Assuming this is an 'ACT' status ID

    @Test
    @DisplayName("should create MasterRoot successfully with create factory method")
    void create_shouldCreateMasterRootSuccessfully() {
        MasterRoot masterRoot = MasterRoot.create(SAMPLE_SHORT_NAME, SAMPLE_FULL_NAME, SAMPLE_PARENT_ID, SAMPLE_STATUS_ID);

        assertThat(masterRoot).isNotNull();
        assertThat(masterRoot.getId()).isNull(); // ID should be null for new creation
        assertThat(masterRoot.getShortName()).isEqualTo(SAMPLE_SHORT_NAME);
        assertThat(masterRoot.getFullName()).isEqualTo(SAMPLE_FULL_NAME);
        assertThat(masterRoot.getParentId()).isEqualTo(SAMPLE_PARENT_ID);
        assertThat(masterRoot.getStatusId()).isEqualTo(SAMPLE_STATUS_ID);
    }

    @Test
    @DisplayName("create should throw RuntimeException if shortName is blank")
    void create_shouldThrowRuntimeExceptionIfShortNameIsBlank() {
        assertThatThrownBy(() -> MasterRoot.create("", SAMPLE_FULL_NAME, SAMPLE_PARENT_ID, SAMPLE_STATUS_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("shortName requerido");
    }

    @Test
    @DisplayName("create should throw RuntimeException if fullName is blank")
    void create_shouldThrowRuntimeExceptionIfFullNameIsBlank() {
        assertThatThrownBy(() -> MasterRoot.create(SAMPLE_SHORT_NAME, "", SAMPLE_PARENT_ID, SAMPLE_STATUS_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("fullName requerido");
    }

    @Test
    @DisplayName("should create MasterRoot successfully with fromPersistence factory method")
    void fromPersistence_shouldCreateMasterRootSuccessfully() {
        MasterRoot masterRoot = MasterRoot.fromPersistence(SAMPLE_ID, SAMPLE_SHORT_NAME, SAMPLE_FULL_NAME, SAMPLE_PARENT_ID, SAMPLE_STATUS_ID);

        assertThat(masterRoot).isNotNull();
        assertThat(masterRoot.getId()).isEqualTo(SAMPLE_ID);
        assertThat(masterRoot.getShortName()).isEqualTo(SAMPLE_SHORT_NAME);
        assertThat(masterRoot.getFullName()).isEqualTo(SAMPLE_FULL_NAME);
        assertThat(masterRoot.getParentId()).isEqualTo(SAMPLE_PARENT_ID);
        assertThat(masterRoot.getStatusId()).isEqualTo(SAMPLE_STATUS_ID);
    }

    @Test
    @DisplayName("fromPersistence should throw IllegalStateException if shortName is blank")
    void fromPersistence_shouldThrowIllegalStateExceptionIfShortNameIsBlank() {
        assertThatThrownBy(() -> MasterRoot.fromPersistence(SAMPLE_ID, "", SAMPLE_FULL_NAME, SAMPLE_PARENT_ID, SAMPLE_STATUS_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Data corrupta en BD: shortName null");
    }

    @Test
    @DisplayName("fromPersistence should throw IllegalStateException if fullName is blank")
    void fromPersistence_shouldThrowIllegalStateExceptionIfFullNameIsBlank() {
        assertThatThrownBy(() -> MasterRoot.fromPersistence(SAMPLE_ID, SAMPLE_SHORT_NAME, "", SAMPLE_PARENT_ID, SAMPLE_STATUS_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Data corrupta en BD: fullName null");
    }

    @Test
    @DisplayName("fromPersistence should throw IllegalStateException if id is null")
    void fromPersistence_shouldThrowIllegalStateExceptionIfIdIsNull() {
        assertThatThrownBy(() -> MasterRoot.fromPersistence(null, SAMPLE_SHORT_NAME, SAMPLE_FULL_NAME, SAMPLE_PARENT_ID, SAMPLE_STATUS_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Data corrupta en BD: id null");
    }

    @Test
    @DisplayName("changeStatus should update statusId if newStatusId is different")
    void changeStatus_shouldUpdateStatusId() {
        MasterRoot masterRoot = new MasterRoot(SAMPLE_ID, SAMPLE_SHORT_NAME, SAMPLE_FULL_NAME, SAMPLE_PARENT_ID, 1L); // Initial status 1L
        Long newStatus = 2L;

        masterRoot.changeStatus(newStatus);

        assertThat(masterRoot.getStatusId()).isEqualTo(newStatus);
    }

    @Test
    @DisplayName("changeStatus should throw DomainValidationException if newStatusId is null")
    void changeStatus_shouldThrowDomainValidationExceptionIfNewStatusIdIsNull() {
        MasterRoot masterRoot = new MasterRoot(SAMPLE_ID, SAMPLE_SHORT_NAME, SAMPLE_FULL_NAME, SAMPLE_PARENT_ID, 1L);

        assertThatThrownBy(() -> masterRoot.changeStatus(null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Status requerido");
    }

    @Test
    @DisplayName("changeStatus should throw DomainValidationException if newStatusId is same as current")
    void changeStatus_shouldThrowDomainValidationExceptionIfNewStatusIdIsSameAsCurrent() {
        MasterRoot masterRoot = new MasterRoot(SAMPLE_ID, SAMPLE_SHORT_NAME, SAMPLE_FULL_NAME, SAMPLE_PARENT_ID, 1L);
        Long sameStatus = 1L;

        assertThatThrownBy(() -> masterRoot.changeStatus(sameStatus))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Ya tiene ese status");
    }

    @Test
    @DisplayName("is should return true if shortName matches code")
    void is_shouldReturnTrueIfShortNameMatchesCode() {
        MasterRoot masterRoot = new MasterRoot(SAMPLE_ID, "ACT", SAMPLE_FULL_NAME, SAMPLE_PARENT_ID, SAMPLE_STATUS_ID);
        assertThat(masterRoot.is("ACT")).isTrue();
    }

    @Test
    @DisplayName("is should return false if shortName does not match code")
    void is_shouldReturnFalseIfShortNameDoesNotMatchCode() {
        MasterRoot masterRoot = new MasterRoot(SAMPLE_ID, "ACT", SAMPLE_FULL_NAME, SAMPLE_PARENT_ID, SAMPLE_STATUS_ID);
        assertThat(masterRoot.is("INA")).isFalse();
    }

    @Test
    @DisplayName("isActive should return true if statusCode is ACT")
    void isActive_shouldReturnTrueIfStatusCodeIsAct() {
        MasterRoot masterRoot = new MasterRoot(SAMPLE_ID, SAMPLE_SHORT_NAME, SAMPLE_FULL_NAME, SAMPLE_PARENT_ID, SAMPLE_STATUS_ID);
        assertThat(masterRoot.isActive("ACT")).isTrue();
    }

    @Test
    @DisplayName("isActive should return false if statusCode is not ACT")
    void isActive_shouldReturnFalseIfStatusCodeIsNotAct() {
        MasterRoot masterRoot = new MasterRoot(SAMPLE_ID, SAMPLE_SHORT_NAME, SAMPLE_FULL_NAME, SAMPLE_PARENT_ID, SAMPLE_STATUS_ID);
        assertThat(masterRoot.isActive("INA")).isFalse();
    }

    @Test
    @DisplayName("canDisable should return true if statusCode is ACT and not INACT or BLOK")
    void canDisable_shouldReturnTrueIfStatusCodeIsActAndNotInactiveOrBlocked() {
        MasterRoot masterRoot = new MasterRoot(SAMPLE_ID, SAMPLE_SHORT_NAME, SAMPLE_FULL_NAME, SAMPLE_PARENT_ID, SAMPLE_STATUS_ID);
        assertThat(masterRoot.canDisable("ACT")).isTrue();
    }

    @Test
    @DisplayName("canDisable should return false if statusCode is INACT")
    void canDisable_shouldReturnFalseIfStatusCodeIsInactive() {
        MasterRoot masterRoot = new MasterRoot(SAMPLE_ID, SAMPLE_SHORT_NAME, SAMPLE_FULL_NAME, SAMPLE_PARENT_ID, SAMPLE_STATUS_ID);
        assertThat(masterRoot.canDisable("INACT")).isFalse();
    }

    @Test
    @DisplayName("canDisable should return false if statusCode is BLOK")
    void canDisable_shouldReturnFalseIfStatusCodeIsBlocked() {
        MasterRoot masterRoot = new MasterRoot(SAMPLE_ID, SAMPLE_SHORT_NAME, SAMPLE_FULL_NAME, SAMPLE_PARENT_ID, SAMPLE_STATUS_ID);
        assertThat(masterRoot.canDisable("BLOK")).isFalse();
    }
}
