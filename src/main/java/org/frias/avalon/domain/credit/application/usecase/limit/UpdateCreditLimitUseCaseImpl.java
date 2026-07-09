package org.frias.avalon.domain.credit.application.usecase.limit;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.credit.application.dto.request.UpdateCreditLimitRequest;
import org.frias.avalon.domain.credit.application.dto.response.CreditAccountResponse;
import org.frias.avalon.domain.credit.application.port.CreditRepositoryPort;
import org.frias.avalon.domain.credit.domain.model.CreditAccountDomain;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Case of use implementation to modify and update client credit limit thresholds.
 */
@Service
@RequiredArgsConstructor
public class UpdateCreditLimitUseCaseImpl implements UpdateCreditLimitUseCase {

    private final CreditRepositoryPort creditRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;

    /**
     * Modifies the credit limit of a client credit account.
     *
     * @param request The update limit details.
     * @return The updated credit account response.
     * @throws ResourceNotFoundException If the credit account is not found.
     */
    @Override
    @Transactional
    public CreditAccountResponse execute(UpdateCreditLimitRequest request) {
        CreditAccountDomain account = creditRepositoryPort.findById(request.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta de crédito no encontrada con ID: " + request.accountId()));

        account.updateLimit(request.newLimit());
        CreditAccountDomain saved = creditRepositoryPort.save(account);

        PersonDomain client = personRepositoryPort.findById(saved.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Ficha de cliente asociada no encontrada"));

        return new CreditAccountResponse(
                saved.getId(),
                saved.getClientId(),
                client.getFullName(),
                client.getNumberid(),
                saved.getOutletId(),
                saved.getCreditLimit(),
                saved.getCurrentDebt(),
                "ACTIVO",
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}
