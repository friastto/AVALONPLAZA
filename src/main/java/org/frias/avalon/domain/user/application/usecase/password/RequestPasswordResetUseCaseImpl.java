package org.frias.avalon.domain.user.application.usecase.password;

import org.frias.avalon.core.validation.EmailMasker;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.request.ForgotPasswordRequestDto;
import org.frias.avalon.domain.user.application.dtos.response.ForgotPasswordResponseDto;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RequestPasswordResetUseCaseImpl implements RequestPasswordResetUseCase {

    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;

    public RequestPasswordResetUseCaseImpl(UserAvalonRepositoryPort userAvalonRepositoryPort, PersonRepositoryPort personRepositoryPort) {
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
        this.personRepositoryPort = personRepositoryPort;
    }

    @Override
    public ForgotPasswordResponseDto execute(ForgotPasswordRequestDto request) {
        Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findByIdentifier(request.identifier());

        if (userOpt.isEmpty() || userOpt.get().getPersonId() == null) {
            return new ForgotPasswordResponseDto(null);
        }

        Optional<PersonDomain> personOpt = personRepositoryPort.findById(userOpt.get().getPersonId());
        if (personOpt.isEmpty() || personOpt.get().getEmail() == null || personOpt.get().getEmail().isBlank()) {
            return new ForgotPasswordResponseDto(null);
        }

        String email = personOpt.get().getEmail();

        // La única responsabilidad de este caso de uso ahora es devolver la pista.
        return new ForgotPasswordResponseDto(EmailMasker.mask(email));
    }
}