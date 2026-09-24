package org.frias.avalon.domain.user.application.usecase.applicant;

import org.frias.avalon.core.validation.EmailMasker;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.request.ApplicantLookupRequestDto;
import org.frias.avalon.domain.user.application.dtos.response.ApplicantLookupResponseDto;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ApplicantLookupUseCaseImpl implements ApplicantLookupUseCase {

    private final PersonRepositoryPort personRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;

    public ApplicantLookupUseCaseImpl(
            PersonRepositoryPort personRepositoryPort,
            UserAvalonRepositoryPort userAvalonRepositoryPort
    ) {
        this.personRepositoryPort = personRepositoryPort;
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicantLookupResponseDto execute(ApplicantLookupRequestDto request) {
        String idNumber = request.identificationNumber().trim();

        Optional<PersonDomain> personOpt = personRepositoryPort.findByNumberid(idNumber);
        if (personOpt.isEmpty()) {
            return new ApplicantLookupResponseDto(false, null);
        }

        Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findByPersonNumberid(idNumber)
                .or(() -> userAvalonRepositoryPort.findByIdentifier(idNumber));

        if (userOpt.isEmpty()) {
            return new ApplicantLookupResponseDto(false, null);
        }

        String rawEmail = personOpt.get().getEmail();
        String maskedEmail = (rawEmail != null && !rawEmail.isBlank())
                ? EmailMasker.mask(rawEmail)
                : null;

        return new ApplicantLookupResponseDto(true, maskedEmail);
    }
}
