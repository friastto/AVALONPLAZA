package org.frias.avalon.domain.pqrs.application.port;

import org.frias.avalon.domain.pqrs.domain.PqrsDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PqrsRepositoryPort {
    PqrsDomain save(PqrsDomain pqrs);
    Optional<PqrsDomain> findById(Long id);
    Optional<PqrsDomain> findByTicketNumber(String ticketNumber);
    Page<PqrsDomain> findAll(String statusCode, String typeCode, String search, Pageable pageable);
    Page<PqrsDomain> findByUserId(Long userId, Pageable pageable);
    long countByStatusCode(String statusCode);
}
