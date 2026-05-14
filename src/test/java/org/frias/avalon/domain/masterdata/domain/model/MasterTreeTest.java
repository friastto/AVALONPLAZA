package org.frias.avalon.domain.masterdata.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MasterTreeTest {

    private MasterRoot rootSts; // ID 1, shortName "ROOTSTS"
    private MasterRoot stsGen;  // ID 2, shortName "STSGEN", parentId 1
    private MasterRoot act;     // ID 3, shortName "ACT", parentId 2
    private MasterRoot ina;     // ID 4, shortName "INA", parentId 2
    private MasterRoot usrSts;  // ID 5, shortName "USR_STS", parentId 1
    private MasterRoot lokUser; // ID 6, shortName "LOKUSER", parentId 5
    private MasterRoot gen;     // ID 7, shortName "GEN" (another root)
    private MasterRoot m;       // ID 8, shortName "M", parentId 7

    private MasterTree masterTree;

    @BeforeEach
    void setUp() {
        // Setup a hierarchy:
        // ROOTSTS (1)
        //   - STSGEN (2)
        //     - ACT (3)
        //     - INA (4)
        //   - USR_STS (5)
        //     - LOKUSER (6)
        // GEN (7)
        //   - M (8)

        rootSts = new MasterRoot(1L, "ROOTSTS", "Type Estados", null, 3L); // statusId 3L for ACT
        stsGen = new MasterRoot(2L, "STSGEN", "Estados Generals", 1L, 3L);
        act = new MasterRoot(3L, "ACT", "Activo", 2L, 3L);
        ina = new MasterRoot(4L, "INA", "Inactivo", 2L, 3L);
        usrSts = new MasterRoot(5L, "USR_STS", "STS Users", 1L, 3L);
        lokUser = new MasterRoot(6L, "LOKUSER", "Bloqueado", 5L, 3L);
        gen = new MasterRoot(7L, "GEN", "Type Genero", null, 3L);
        m = new MasterRoot(8L, "M", "Masculino", 7L, 3L);

        List<MasterRoot> masterRootList = Arrays.asList(rootSts, stsGen, act, ina, usrSts, lokUser, gen, m);
        masterTree = new MasterTree(masterRootList);
    }

    @Test
    @DisplayName("constructor should correctly build internal maps")
    void constructor_shouldBuildInternalMaps() {
        assertThat(masterTree).isNotNull();
        assertThat(masterTree.getById(rootSts.getId())).isEqualTo(rootSts);
        assertThat(masterTree.getByCode(rootSts.getShortName())).isEqualTo(rootSts);
        assertThat(masterTree.getById(act.getId())).isEqualTo(act);
        assertThat(masterTree.getByCode(act.getShortName())).isEqualTo(act);
    }

    @Test
    @DisplayName("isChildOf should return true for direct child")
    void isChildOf_shouldReturnTrueForDirectChild() {
        assertThat(masterTree.isChildOf(act, "STSGEN")).isTrue();
    }

    @Test
    @DisplayName("isChildOf should return true for grandchild")
    void isChildOf_shouldReturnTrueForGrandchild() {
        assertThat(masterTree.isChildOf(act, "ROOTSTS")).isTrue();
    }

    @Test
    @DisplayName("isChildOf should return false if not a child")
    void isChildOf_shouldReturnFalseIfNotAChild() {
        assertThat(masterTree.isChildOf(act, "USR_STS")).isFalse();
    }

    @Test
    @DisplayName("isChildOf should return false if parentCode is not found")
    void isChildOf_shouldReturnFalseIfParentCodeIsNotFound() {
        assertThat(masterTree.isChildOf(act, "NONEXISTENT")).isFalse();
    }

    @Test
    @DisplayName("isChildOf should return true if node's shortName matches parentCode (including self)")
    void isChildOf_shouldReturnTrueForSelfMatchingParentCode() { // Renombrado para mayor claridad
        assertThat(masterTree.isChildOf(act, "ACT")).isTrue(); // Corregido a true
    }

    @Test
    @DisplayName("isChildOf should return true if node is a child of its own parent (direct parent)")
    void isChildOf_shouldReturnTrueIfNodeIsChildOfItsOwnParent() {
        assertThat(masterTree.isChildOf(stsGen, "ROOTSTS")).isTrue();
    }

    @Test
    @DisplayName("getById should return MasterRoot if found")
    void getById_shouldReturnMasterRootIfFound() {
        assertThat(masterTree.getById(act.getId())).isEqualTo(act);
    }

    @Test
    @DisplayName("getById should return null if not found")
    void getById_shouldReturnNullIfNotFound() {
        assertThat(masterTree.getById(99L)).isNull();
    }

    @Test
    @DisplayName("getByIdOrThrow should return MasterRoot if found")
    void getByIdOrThrow_shouldReturnMasterRootIfFound() {
        assertThat(masterTree.getByIdOrThrow(act.getId())).isEqualTo(act);
    }

    @Test
    @DisplayName("getByIdOrThrow should throw IllegalStateException if not found")
    void getByIdOrThrow_shouldThrowIllegalStateExceptionIfNotFound() {
        assertThatThrownBy(() -> masterTree.getByIdOrThrow(99L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MasterData no encontrado: 99");
    }

    @Test
    @DisplayName("is should return true for matching code (case-insensitive)")
    void is_shouldReturnTrueForMatchingCodeCaseInsensitive() {
        assertThat(masterTree.is(act, "act")).isTrue();
        assertThat(masterTree.is(act, "ACT")).isTrue();
    }

    @Test
    @DisplayName("is should return false for non-matching code")
    void is_shouldReturnFalseForNonMatchingCode() {
        assertThat(masterTree.is(act, "INA")).isFalse();
    }

    @Test
    @DisplayName("is should return false if node is null")
    void is_shouldReturnFalseIfNodeIsNull() {
        assertThat(masterTree.is(null, "ACT")).isFalse();
    }

    @Test
    @DisplayName("is should return false if code is null")
    void is_shouldReturnFalseIfCodeIsNull() {
        assertThat(masterTree.is(act, null)).isFalse();
    }

    @Test
    @DisplayName("getByCode should return MasterRoot if found (case-insensitive)")
    void getByCode_shouldReturnMasterRootIfFoundCaseInsensitive() {
        assertThat(masterTree.getByCode("act")).isEqualTo(act);
        assertThat(masterTree.getByCode("ACT")).isEqualTo(act);
    }

    @Test
    @DisplayName("getByCode should return null if not found")
    void getByCode_shouldReturnNullIfNotFound() {
        assertThat(masterTree.getByCode("NONEXISTENT")).isNull();
    }

    @Test
    @DisplayName("getByCode should return null if code is null")
    void getByCode_shouldReturnNullIfCodeIsNull() {
        assertThat(masterTree.getByCode(null)).isNull();
    }

    @Test
    @DisplayName("isAny should return true if node matches any of the codes")
    void isAny_shouldReturnTrueIfNodeMatchesAnyCode() {
        assertThat(masterTree.isAny(act, "INA", "ACT", "DEL")).isTrue();
    }

    @Test
    @DisplayName("isAny should return false if node does not match any of the codes")
    void isAny_shouldReturnFalseIfNodeDoesNotMatchAnyCode() {
        assertThat(masterTree.isAny(act, "INA", "DEL")).isFalse();
    }

    @Test
    @DisplayName("isAny should return false if node is null")
    void isAny_shouldReturnFalseIfNodeIsNull() {
        assertThat(masterTree.isAny(null, "ACT")).isFalse();
    }

    @Test
    @DisplayName("isAny should return false if codes are null")
    void isAny_shouldReturnFalseIfCodesAreNull() {
        assertThat(masterTree.isAny(act, (String[]) null)).isFalse();
    }
}
