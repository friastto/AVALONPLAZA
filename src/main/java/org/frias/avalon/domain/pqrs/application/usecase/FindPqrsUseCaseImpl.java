package org.frias.avalon.domain.pqrs.application.usecase;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.pqrs.application.dto.response.PqrsResponse;
import org.frias.avalon.domain.pqrs.application.dto.response.PqrsResponseMapper;
import org.frias.avalon.domain.pqrs.application.port.PqrsRepositoryPort;
import org.frias.avalon.domain.pqrs.domain.PqrsDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class FindPqrsUseCaseImpl implements FindPqrsUseCase {

    private final PqrsRepositoryPort pqrsRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final PqrsResponseMapper pqrsResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PqrsResponse> findAll(String statusCode, String typeCode, String search, Pageable pageable) {
        Page<PqrsDomain> domains = pqrsRepositoryPort.findAll(statusCode, typeCode, search, pageable);
        return domains.map(this::mapDomainToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PqrsResponse findById(Long id) {
        PqrsDomain domain = pqrsRepositoryPort.findById(id)
                .orElseThrow(() -> new NoSuchElementException("PQRS con ID " + id + " no encontrado"));
        return mapDomainToResponse(domain);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPqrsStats() {
        long pending = pqrsRepositoryPort.countByStatusCode("PEN");
        long inProgress = pqrsRepositoryPort.countByStatusCode("PRO");
        long resolved = pqrsRepositoryPort.countByStatusCode("RES");
        long closed = pqrsRepositoryPort.countByStatusCode("CER");
        long total = pending + inProgress + resolved + closed;

        Map<String, Object> stats = new HashMap<>();
        stats.put("pending", pending);
        stats.put("inProgress", inProgress);
        stats.put("resolved", resolved);
        stats.put("closed", closed);
        stats.put("total", total);
        return stats;
    }

    private PqrsResponse mapDomainToResponse(PqrsDomain domain) {
        String userName = null;
        if (domain.getUserId() != null) {
            userName = userAvalonRepositoryPort.findById(domain.getUserId())
                    .map(UserAvalonDomain::getUserName)
                    .orElse(null);
        }

        String respondedByName = null;
        if (domain.getRespondedByUserId() != null) {
            respondedByName = userAvalonRepositoryPort.findById(domain.getRespondedByUserId())
                    .map(UserAvalonDomain::getUserName)
                    .orElse(null);
        }

        return pqrsResponseMapper.toResponse(domain, userName, respondedByName);
    }
}
