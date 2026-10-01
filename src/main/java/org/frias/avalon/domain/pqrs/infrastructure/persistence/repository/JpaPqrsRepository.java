package org.frias.avalon.domain.pqrs.infrastructure.persistence.repository;

import org.frias.avalon.domain.pqrs.infrastructure.persistence.entity.PqrsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaPqrsRepository extends JpaRepository<PqrsEntity, Long>, JpaSpecificationExecutor<PqrsEntity> {
    Optional<PqrsEntity> findByTicketNumber(String ticketNumber);
    long countByStatusCode(String statusCode);
}
