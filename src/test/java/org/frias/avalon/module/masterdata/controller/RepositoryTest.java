
package org.frias.avalon.module.masterdata.controller;

import org.frias.avalon.domain.masterdata.infraestructure.persistence.entity.MasterData;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.repository.MasterDataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Sql("/org/frias/avalon/module/masterdata/master-data.sql")
class RepositoryTest {


    private final MasterDataRepository repository;

    RepositoryTest(MasterDataRepository repository) {
        this.repository = repository;
    }

    @Test
    void searchByShortName() {

        Optional<MasterData> p = repository.findByShortNameAndStatusActive("ACT");

        assertTrue(p.isPresent());
    }
}
