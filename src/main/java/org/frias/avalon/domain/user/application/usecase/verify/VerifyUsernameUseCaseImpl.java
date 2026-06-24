package org.frias.avalon.domain.user.application.usecase.verify;

import org.frias.avalon.domain.person.application.dto.response.VerificationResponseDto;
import org.frias.avalon.domain.user.application.dtos.request.VerifyUsernameRequestDto;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class VerifyUsernameUseCaseImpl implements VerifyUsernameUseCase {

    private final UserAvalonRepositoryPort userAvalonRepositoryPort;

    public VerifyUsernameUseCaseImpl(UserAvalonRepositoryPort userAvalonRepositoryPort) {
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
    }

    @Override
    public VerificationResponseDto execute(VerifyUsernameRequestDto request) {
        boolean userExists = userAvalonRepositoryPort.findByIdentifier(request.userName()).isPresent();
        
        // Reutilizamos el DTO de respuesta, ajustando los campos según el contexto.
        return new VerificationResponseDto(null, userExists, null);
    }
}