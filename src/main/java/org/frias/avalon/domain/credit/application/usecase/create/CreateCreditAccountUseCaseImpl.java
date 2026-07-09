package org.frias.avalon.domain.credit.application.usecase.create;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.credit.application.dto.request.CreateCreditAccountRequest;
import org.frias.avalon.domain.credit.application.dto.response.CreditAccountResponse;
import org.frias.avalon.domain.credit.application.port.CreditRepositoryPort;
import org.frias.avalon.domain.credit.domain.model.CreditAccountDomain;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Case of use implementation to initialize and configure credit accounts for trusted clients.
 */
@Service
@RequiredArgsConstructor
public class CreateCreditAccountUseCaseImpl implements CreateCreditAccountUseCase {

    private final CreditRepositoryPort creditRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;

    /**
     * Executes the credit account creation flow for a specific client.
     *
     * @param request The credit creation request details.
     * @return The response DTO containing credit details.
     * @throws ResourceNotFoundException If the client person register is not found.
     * @throws BusinessException If credit account already exists.
     */
    @Override
    @Transactional
    public CreditAccountResponse execute(CreateCreditAccountRequest request) {
        PersonDomain client = personRepositoryPort.findByNumberid(request.clientNumberid())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con identificación: " + request.clientNumberid()));

        creditRepositoryPort.findByClientIdAndOutletId(client.getId(), request.outletId())
                .ifPresent(acc -> {
                    throw new BusinessException("El cliente ya cuenta con crédito configurado en este establecimiento");
                });

        Long activeStatusId = masterDataRepositoryPort.getIdByCode("ACT");
        if (activeStatusId == null) {
            throw new IllegalStateException("Estado Activo ('ACT') no encontrado en MasterData");
        }

        CreditAccountDomain account = CreditAccountDomain.create(
                client.getId(),
                request.outletId(),
                request.creditLimit(),
                activeStatusId
        );

        CreditAccountDomain saved = creditRepositoryPort.save(account);

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
