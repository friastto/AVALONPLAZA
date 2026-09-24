package org.frias.avalon.domain.user.application.usecase.applicant;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.request.ApplicantVerifyPinRequestDto;
import org.frias.avalon.domain.user.application.dtos.response.ApplicantVerifiedResponseDto;
import org.frias.avalon.domain.user.domain.model.PasswordResetTokenDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.PasswordResetTokenRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicantVerifyPinUseCaseImpl implements ApplicantVerifyPinUseCase {

    private final PersonRepositoryPort personRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final PasswordResetTokenRepositoryPort tokenRepositoryPort;

    public ApplicantVerifyPinUseCaseImpl(
            PersonRepositoryPort personRepositoryPort,
            UserAvalonRepositoryPort userAvalonRepositoryPort,
            PasswordResetTokenRepositoryPort tokenRepositoryPort
    ) {
        this.personRepositoryPort = personRepositoryPort;
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
        this.tokenRepositoryPort = tokenRepositoryPort;
    }

    @Override
    @Transactional
    public ApplicantVerifiedResponseDto execute(ApplicantVerifyPinRequestDto request) {
        String idNumber = request.identificationNumber().trim();

        UserAvalonDomain user = userAvalonRepositoryPort.findByPersonNumberid(idNumber)
                .or(() -> userAvalonRepositoryPort.findByIdentifier(idNumber))
                .orElseThrow(() -> new BusinessException("PIN o identificacion invalida."));

        PasswordResetTokenDomain token = tokenRepositoryPort.findByUserIdAndPin(user.getId(), request.pin())
                .orElseThrow(() -> new BusinessException("PIN o identificacion invalida."));

        if (token.isExpired()) {
            tokenRepositoryPort.delete(token);
            throw new BusinessException("El codigo PIN ha expirado.");
        }

        PersonDomain person = personRepositoryPort.findById(user.getPersonId())
                .orElseThrow(() -> new BusinessException("Datos de persona asociados no encontrados."));

        String lastName = person.getLastName() != null ? person.getLastName() : "";
        String fullName = (person.getName() + " " + lastName).trim();

        return new ApplicantVerifiedResponseDto(
                user.getId(),
                person.getId(),
                fullName,
                person.getEmail(),
                token.getVerificationToken()
        );
    }
}
