package org.frias.avalon.domain.pqrs.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.pqrs.application.port.PqrsRepositoryPort;
import org.frias.avalon.domain.pqrs.domain.PqrsDomain;
import org.frias.avalon.domain.pqrs.infrastructure.persistence.entity.PqrsEntity;
import org.frias.avalon.domain.pqrs.infrastructure.persistence.mapper.PqrsMapper;
import org.frias.avalon.domain.pqrs.infrastructure.persistence.repository.JpaPqrsRepository;
import org.frias.avalon.domain.pqrs.infrastructure.persistence.specification.PqrsSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PqrsPersistenceAdapter implements PqrsRepositoryPort {

    private final JpaPqrsRepository jpaPqrsRepository;
    private final PqrsMapper pqrsMapper;

    @Override
    public PqrsDomain save(PqrsDomain pqrs) {
        PqrsEntity entity = pqrsMapper.toEntity(pqrs);
        PqrsEntity saved = jpaPqrsRepository.save(entity);
        return pqrsMapper.toDomain(saved);
    }

    @Override
    public Optional<PqrsDomain> findById(Long id) {
        return jpaPqrsRepository.findById(id).map(pqrsMapper::toDomain);
    }

    @Override
    public Optional<PqrsDomain> findByTicketNumber(String ticketNumber) {
        return jpaPqrsRepository.findByTicketNumber(ticketNumber).map(pqrsMapper::toDomain);
    }

    @Override
    public Page<PqrsDomain> findAll(String statusCode, String typeCode, String search, Pageable pageable) {
        Specification<PqrsEntity> spec = Specification.allOf(
                PqrsSpecification.hasStatusCode(statusCode),
                PqrsSpecification.hasTypeCode(typeCode),
                PqrsSpecification.hasSearchText(search)
        );
        return jpaPqrsRepository.findAll(spec, pageable).map(pqrsMapper::toDomain);
    }

    @Override
    public Page<PqrsDomain> findByUserId(Long userId, Pageable pageable) {
        Specification<PqrsEntity> spec = PqrsSpecification.hasUserId(userId);
        return jpaPqrsRepository.findAll(spec, pageable).map(pqrsMapper::toDomain);
    }

    @Override
    public long countByStatusCode(String statusCode) {
        return jpaPqrsRepository.countByStatusCode(statusCode);
    }
}
