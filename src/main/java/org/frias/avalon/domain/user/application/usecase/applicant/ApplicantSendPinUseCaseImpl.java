package org.frias.avalon.domain.user.application.usecase.applicant;

import org.frias.avalon.core.notification.EmailServicePort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.request.ApplicantSendPinRequestDto;
import org.frias.avalon.domain.user.domain.model.PasswordResetTokenDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.PasswordResetTokenRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ApplicantSendPinUseCaseImpl implements ApplicantSendPinUseCase {

    private final PersonRepositoryPort personRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final PasswordResetTokenRepositoryPort tokenRepositoryPort;
    private final EmailServicePort emailServicePort;

    public ApplicantSendPinUseCaseImpl(
            PersonRepositoryPort personRepositoryPort,
            UserAvalonRepositoryPort userAvalonRepositoryPort,
            PasswordResetTokenRepositoryPort tokenRepositoryPort,
            EmailServicePort emailServicePort
    ) {
        this.personRepositoryPort = personRepositoryPort;
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
        this.tokenRepositoryPort = tokenRepositoryPort;
        this.emailServicePort = emailServicePort;
    }

    @Override
    @Transactional
    public void execute(ApplicantSendPinRequestDto request) {
        String idNumber = request.identificationNumber().trim();

        Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findByPersonNumberid(idNumber)
                .or(() -> userAvalonRepositoryPort.findByIdentifier(idNumber));

        if (userOpt.isEmpty()) {
            return; // Silent finish to prevent enumeration
        }

        UserAvalonDomain user = userOpt.get();
        if (user.getPersonId() == null) {
            return;
        }

        Optional<PersonDomain> personOpt = personRepositoryPort.findById(user.getPersonId());
        if (personOpt.isEmpty() || personOpt.get().getEmail() == null || personOpt.get().getEmail().isBlank()) {
            return;
        }

        PasswordResetTokenDomain token = PasswordResetTokenDomain.create(user.getId());
        tokenRepositoryPort.save(token);

        emailServicePort.sendApplicantVerificationPin(personOpt.get().getEmail(), token.getPin());
    }
}
