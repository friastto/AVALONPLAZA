package org.frias.avalon.domain.pqrs.application.usecase;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.jwt.util.SecurityUtils;
import org.frias.avalon.domain.pqrs.application.dto.request.CreatePqrsRequest;
import org.frias.avalon.domain.pqrs.application.dto.response.PqrsResponse;
import org.frias.avalon.domain.pqrs.application.dto.response.PqrsResponseMapper;
import org.frias.avalon.domain.pqrs.application.port.PqrsRepositoryPort;
import org.frias.avalon.domain.pqrs.domain.PqrsDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CreatePqrsUseCaseImpl implements CreatePqrsUseCase {

    private final PqrsRepositoryPort pqrsRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final PqrsResponseMapper pqrsResponseMapper;

    @Override
    @Transactional
    public PqrsResponse execute(CreatePqrsRequest request) {
        String currentLogin = SecurityUtils.getCurrentUserLogin();
        Long userId = null;
        String userName = null;

        if (currentLogin != null && !currentLogin.isBlank() && !"anonymousUser".equalsIgnoreCase(currentLogin)) {
            Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findByUserName(currentLogin);
            if (userOpt.isPresent()) {
                userId = userOpt.get().getId();
                userName = userOpt.get().getUserName();
            }
        }

        String ticketNumber = generateUniqueTicketNumber();
        String normalizedType = PqrsResponseMapper.normalizeTypeCode(request.getTypeCode());

        PqrsDomain domain = PqrsDomain.builder()
                .ticketNumber(ticketNumber)
                .userId(userId)
                .orderId(request.getOrderId())
                .storeId(request.getStoreId())
                .typeCode(normalizedType)
                .statusCode("PEN")
                .subject(request.getSubject().trim())
                .description(request.getDescription().trim())
                .contactEmail(request.getContactEmail() != null ? request.getContactEmail().trim() : null)
                .contactPhone(request.getContactPhone() != null ? request.getContactPhone().trim() : null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PqrsDomain saved = pqrsRepositoryPort.save(domain);
        return pqrsResponseMapper.toResponse(saved, userName, null);
    }

    private String generateUniqueTicketNumber() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        for (int i = 0; i < 10; i++) {
            int randomSuffix = ThreadLocalRandom.current().nextInt(10000, 99999);
            String candidate = "PQRS-" + datePrefix + "-" + randomSuffix;
            if (pqrsRepositoryPort.findByTicketNumber(candidate).isEmpty()) {
                return candidate;
            }
        }
        return "PQRS-" + datePrefix + "-" + System.currentTimeMillis() % 100000;
    }
}
