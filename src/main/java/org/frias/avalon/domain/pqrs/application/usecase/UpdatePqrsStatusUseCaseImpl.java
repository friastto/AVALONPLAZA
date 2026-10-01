package org.frias.avalon.domain.pqrs.application.usecase;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.jwt.util.SecurityUtils;
import org.frias.avalon.domain.pqrs.application.dto.request.UpdatePqrsStatusRequest;
import org.frias.avalon.domain.pqrs.application.dto.response.PqrsResponse;
import org.frias.avalon.domain.pqrs.application.dto.response.PqrsResponseMapper;
import org.frias.avalon.domain.pqrs.application.port.PqrsRepositoryPort;
import org.frias.avalon.domain.pqrs.domain.PqrsDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpdatePqrsStatusUseCaseImpl implements UpdatePqrsStatusUseCase {

    private final PqrsRepositoryPort pqrsRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final PqrsResponseMapper pqrsResponseMapper;

    @Override
    @Transactional
    public PqrsResponse execute(Long id, UpdatePqrsStatusRequest request) {
        PqrsDomain existing = pqrsRepositoryPort.findById(id)
                .orElseThrow(() -> new NoSuchElementException("PQRS con ID " + id + " no encontrado"));

        String currentLogin = SecurityUtils.getCurrentUserLogin();
        Long adminUserId = null;
        String adminUserName = null;

        if (currentLogin != null && !currentLogin.isBlank()) {
            Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findByUserName(currentLogin);
            if (userOpt.isPresent()) {
                adminUserId = userOpt.get().getId();
                adminUserName = userOpt.get().getUserName();
            }
        }

        String newStatus = request.getStatusCode() != null ? request.getStatusCode().trim().toUpperCase() : "PEN";
        if (request.getAdminNotes() != null && !request.getAdminNotes().isBlank()) {
            existing.setAdminNotes(request.getAdminNotes().trim());
        }

        existing.setStatusCode(newStatus);
        if (adminUserId != null) {
            existing.setRespondedByUserId(adminUserId);
        }
        existing.setUpdatedAt(LocalDateTime.now());

        PqrsDomain updated = pqrsRepositoryPort.save(existing);

        String creatorName = null;
        if (updated.getUserId() != null) {
            creatorName = userAvalonRepositoryPort.findById(updated.getUserId())
                    .map(UserAvalonDomain::getUserName)
                    .orElse(null);
        }

        return pqrsResponseMapper.toResponse(updated, creatorName, adminUserName);
    }
}
