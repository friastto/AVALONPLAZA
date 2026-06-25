package org.frias.avalon.domain.user.application.usecase.password;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.user.application.dtos.request.ResetPasswordRequestDto;
import org.frias.avalon.domain.user.domain.model.PasswordResetTokenDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.PasswordResetTokenRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResetPasswordUseCaseImpl implements ResetPasswordUseCase {

    private final PasswordResetTokenRepositoryPort tokenRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;

    public ResetPasswordUseCaseImpl(PasswordResetTokenRepositoryPort tokenRepositoryPort, UserAvalonRepositoryPort userAvalonRepositoryPort) {
        this.tokenRepositoryPort = tokenRepositoryPort;
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
    }

    @Override
    @Transactional
    public void execute(ResetPasswordRequestDto request) {
        // 1. Validar el token de verificación
        PasswordResetTokenDomain token = tokenRepositoryPort.findByVerificationToken(request.verificationToken())
                .orElseThrow(() -> new BusinessException("El token de restablecimiento no es válido o ha expirado."));

        if (token.isExpired()) {
            tokenRepositoryPort.delete(token); // Limpiar token expirado
            throw new BusinessException("El token de restablecimiento ha expirado.");
        }

        // 2. Encontrar al usuario asociado
        UserAvalonDomain user = userAvalonRepositoryPort.findById(token.getUserId())
                .orElseThrow(() -> new BusinessException("No se pudo encontrar el usuario asociado al token."));

        // 3. Ordenar al dominio que cambie la contraseña
        user.changePassword(request.newPassword());
        
        // 4. Guardar el estado actualizado del usuario
        userAvalonRepositoryPort.save(user);

        // 5. Invalidar el token eliminándolo
        tokenRepositoryPort.delete(token);
    }
}