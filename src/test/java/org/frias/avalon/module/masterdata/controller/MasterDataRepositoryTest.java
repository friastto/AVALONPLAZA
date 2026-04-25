package org.frias.avalon.module.masterdata.controller;

import org.frias.avalon.domain.masterdata.infraestructure.persistence.entity.MasterData;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.repository.MasterDataRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Sql("/data-h2.sql") // Carga nuestros datos antes de cada test
@Transactional // IMPORTANTE: Revierte los cambios después de cada test
class MasterDataRepositoryTest {

    @Autowired
    private MasterDataRepository repository;

    @Test
    @DisplayName("Debe encontrar MasterData por shortName si está ACTIVO")
    void shouldFindActiveByShortName() {
        Optional<MasterData> result = repository.findByShortNameAndStatusActive("ELEC");

        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("ELECTRONICA");
    }

    @Test
    @DisplayName("NO debe encontrar MasterData si está INACTIVO")
    void shouldNotFindInactiveByShortName() {
        // LAP está con status_id=3 (INACTIVO en nuestro SQL)
        Optional<MasterData> result = repository.findByShortNameAndStatusActive("LAP");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Debe traer todos los hijos activos dado un ShortName padre")
    void shouldFindChildrenByParentShortName() {
        // CAT_PROD tiene hijos: ELEC y ROPA (activos)
        List<MasterData> children = repository.findAllChildrenByParentShortNameAndActive("CAT_PROD");

        assertThat(children).hasSize(2);
        assertThat(children).extracting(MasterData::getShortName)
                .containsExactlyInAnyOrder("ELEC", "ROPA");
    }
}
