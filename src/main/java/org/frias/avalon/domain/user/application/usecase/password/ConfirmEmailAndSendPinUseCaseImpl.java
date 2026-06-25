package org.frias.avalon.domain.user.application.usecase.password;

import org.frias.avalon.core.notification.EmailServicePort;
import org.frias.avalon.domain.user.application.dtos.request.ConfirmEmailRequestDto;
import org.frias.avalon.domain.user.domain.model.PasswordResetTokenDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.PasswordResetTokenRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ConfirmEmailAndSendPinUseCaseImpl implements ConfirmEmailAndSendPinUseCase {

    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final PasswordResetTokenRepositoryPort tokenRepositoryPort;
    private final EmailServicePort emailServicePort;

    public ConfirmEmailAndSendPinUseCaseImpl(UserAvalonRepositoryPort userAvalonRepositoryPort, PasswordResetTokenRepositoryPort tokenRepositoryPort, EmailServicePort emailServicePort) {
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
        this.tokenRepositoryPort = tokenRepositoryPort;
        this.emailServicePort = emailServicePort;
    }

    @Override
    @Transactional
    public void execute(ConfirmEmailRequestDto request) {
        // Buscamos al usuario por el email completo que el usuario ha confirmado
        Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findByIdentifier(request.email());

        // Si el email es correcto y el usuario existe, procedemos a enviar el PIN.
        // Si no, la ejecución termina silenciosamente para no revelar información.
        if (userOpt.isPresent()) {
            UserAvalonDomain user = userOpt.get();

            // Crear y guardar el token con PIN
            PasswordResetTokenDomain token = PasswordResetTokenDomain.create(user.getId());
            tokenRepositoryPort.save(token);

            // Enviar el PIN por correo
            emailServicePort.sendPasswordResetPin(request.email(), token.getPin());
        }
    }
}