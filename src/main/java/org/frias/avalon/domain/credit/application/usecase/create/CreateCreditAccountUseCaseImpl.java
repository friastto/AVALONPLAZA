package org.frias.avalon.domain.credit.application.usecase.create;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.credit.application.dto.request.CreateCreditAccountRequest;
import org.frias.avalon.domain.credit.application.dto.response.CreditAccountResponse;
import org.frias.avalon.domain.credit.application.port.CreditRepositoryPort;
import org.frias.avalon.domain.credit.domain.model.CreditAccountDomain;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Case of use implementation to initialize and configure credit accounts for trusted clients.
 * Supports multi-tenant schema switching and transactional isolation.
 */
@Service
public class CreateCreditAccountUseCaseImpl implements CreateCreditAccountUseCase {

    private final CreditRepositoryPort creditRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final JpaOutletRepository jpaOutletRepository;
    private final TransactionTemplate transactionTemplate;

    public CreateCreditAccountUseCaseImpl(
            CreditRepositoryPort creditRepositoryPort,
            PersonRepositoryPort personRepositoryPort,
            MasterTreeProvider masterTreeProvider,
            JpaOutletRepository jpaOutletRepository,
            PlatformTransactionManager transactionManager) {
        this.creditRepositoryPort = creditRepositoryPort;
        this.personRepositoryPort = personRepositoryPort;
        this.masterTreeProvider = masterTreeProvider;
        this.jpaOutletRepository = jpaOutletRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * Executes the credit account creation flow for a specific client.
     *
     * @param request The credit creation request details.
     * @return The response DTO containing credit details.
     * @throws ResourceNotFoundException If the client person register is not found.
     * @throws BusinessException If credit account already exists.
     */
    @Override
    public CreditAccountResponse execute(CreateCreditAccountRequest request) {
        Long previousOutletId = TenantContext.getTenantOutletId();
        Long previousTenantId = TenantContext.getTenantId();

        try {
            if (request.outletId() != null) {
                Outlet outlet = jpaOutletRepository.findById(request.outletId())
                        .orElseThrow(() -> new ResourceNotFoundException("La tienda con ID " + request.outletId() + " no existe."));
                if (outlet.getCompanyId() != null) {
                    TenantContext.setTenantId(outlet.getCompanyId());
                }
                TenantContext.setTenantOutletId(outlet.getId());
            }

            return transactionTemplate.execute(status -> {
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
            });
        } finally {
            TenantContext.setTenantId(previousTenantId);
            TenantContext.setTenantOutletId(previousOutletId);
        }
    }
}
