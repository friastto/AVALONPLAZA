package org.frias.avalon.domain.company.infrastructure.repository;

import org.frias.avalon.domain.company.infrastructure.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for CompanyEntity.
 */
public interface JpaCompanyRepository extends JpaRepository<CompanyEntity, Long> {

    Optional<CompanyEntity> findByNit(String nit);

    boolean existsByNit(String nit);
}
