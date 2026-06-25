package org.frias.avalon.domain.user.application.usecase.password;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.user.application.dtos.request.VerifyPinRequestDto;
import org.frias.avalon.domain.user.application.dtos.response.VerifyPinResponseDto;
import org.frias.avalon.domain.user.domain.model.PasswordResetTokenDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.PasswordResetTokenRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerifyPasswordResetPinUseCaseImpl implements VerifyPasswordResetPinUseCase {

    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final PasswordResetTokenRepositoryPort tokenRepositoryPort;

    public VerifyPasswordResetPinUseCaseImpl(UserAvalonRepositoryPort userAvalonRepositoryPort, PasswordResetTokenRepositoryPort tokenRepositoryPort) {
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
        this.tokenRepositoryPort = tokenRepositoryPort;
    }

    @Override
    @Transactional
    public VerifyPinResponseDto execute(VerifyPinRequestDto request) {
        // 1. Encontrar al usuario por su email
        UserAvalonDomain user = userAvalonRepositoryPort.findByIdentifier(request.email())
                .orElseThrow(() -> new BusinessException("PIN o email inválido."));

        // 2. Buscar el token por usuario y PIN
        PasswordResetTokenDomain token = tokenRepositoryPort.findByUserIdAndPin(user.getId(), request.pin())
                .orElseThrow(() -> new BusinessException("PIN o email inválido."));

        // 3. Validar si ha expirado
        if (token.isExpired()) {
            tokenRepositoryPort.delete(token); // Limpiar token expirado
            throw new BusinessException("El código PIN ha expirado.");
        }

        // 4. Devolver el token de verificación seguro
        return new VerifyPinResponseDto(token.getVerificationToken());
    }
}