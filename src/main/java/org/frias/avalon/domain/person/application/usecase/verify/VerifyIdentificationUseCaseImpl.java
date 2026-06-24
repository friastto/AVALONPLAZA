package org.frias.avalon.domain.person.application.usecase.verify;

import org.frias.avalon.domain.person.application.dto.request.VerifyIdentificationRequestDto;
import org.frias.avalon.domain.person.application.dto.response.VerificationResponseDto;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VerifyIdentificationUseCaseImpl implements VerifyIdentificationUseCase {

    private final PersonRepositoryPort personRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;

    public VerifyIdentificationUseCaseImpl(PersonRepositoryPort personRepositoryPort, UserAvalonRepositoryPort userAvalonRepositoryPort) {
        this.personRepositoryPort = personRepositoryPort;

        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
    }

    @Override
    public VerificationResponseDto execute(VerifyIdentificationRequestDto request) {
        Optional<PersonDomain> personOpt = personRepositoryPort.findByNumberid(request.identificationNumber());

        if (personOpt.isEmpty()) {
            return new VerificationResponseDto(false, false, null);
        }


        boolean userExists = userAvalonRepositoryPort.findByIdentifier(request.identificationNumber()).isPresent();

        String nameHint = null;

        return new VerificationResponseDto(true, userExists, nameHint);
    }
}