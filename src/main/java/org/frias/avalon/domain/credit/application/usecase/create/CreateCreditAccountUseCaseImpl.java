package org.frias.avalon.domain.credit.application.usecase.create;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.credit.application.dto.request.CreateCreditAccountRequest;
import org.frias.avalon.domain.credit.application.dto.response.CreditAccountResponse;
import org.frias.avalon.domain.credit.application.port.CreditRepositoryPort;
import org.frias.avalon.domain.credit.domain.model.CreditAccountDomain;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
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
    private final MasterTreeProvider masterTreeProvider;

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
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con identificacion: " + request.clientNumberid()));

        creditRepositoryPort.findByClientIdAndOutletId(client.getId(), request.outletId())
                .ifPresent(acc -> {
                    throw new BusinessException("El cliente ya cuenta con credito configurado en este establecimiento");
                });

        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot activeNode = tree != null ? tree.getByCode("ACT") : null;
        if (activeNode == null) {
            throw new IllegalStateException("Estado Activo ('ACT') no encontrado en MasterData");
        }
        Long activeStatusId = activeNode.getId();

        CreditAccountDomain account = CreditAccountDomain.create(
                client.getId(),
                request.outletId(),
                request.creditLimit(),
                activeStatusId
        );

        CreditAccountDomain saved = creditRepositoryPort.save(account);

        MasterRoot statusNode = tree.getById(saved.getStatusId());
        String statusLabel = (statusNode != null && statusNode.getFullName() != null) ? statusNode.getFullName() : "ACTIVO";

        return new CreditAccountResponse(
                saved.getId(),
                saved.getClientId(),
                client.getFullName(),
                client.getNumberid(),
                saved.getOutletId(),
                saved.getCreditLimit(),
                saved.getCurrentDebt(),
                statusLabel,
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}
