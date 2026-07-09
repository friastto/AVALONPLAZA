package org.frias.avalon.domain.credit.application.usecase.find;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.credit.application.dto.response.CreditAccountResponse;
import org.frias.avalon.domain.credit.application.dto.response.CreditTransactionResponse;
import org.frias.avalon.domain.credit.application.port.CreditRepositoryPort;
import org.frias.avalon.domain.credit.domain.model.CreditAccountDomain;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Case of use implementation to find or initialize credit accounts, ledger history, and listing store debtors.
 */
@Service
@RequiredArgsConstructor
public class FindCreditAccountByClientUseCaseImpl implements FindCreditAccountByClientUseCase {

    private final CreditRepositoryPort creditRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;

    private static final BigDecimal DEFAULT_CREDIT_LIMIT = new BigDecimal("150000");

    /**
     * Resolves a client's credit account, automatically initializing one if none exists.
     */
    @Override
    @Transactional
    public CreditAccountResponse findOrCreate(String clientNumberid, Long outletId) {
        PersonDomain client = personRepositoryPort.findByNumberid(clientNumberid)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con identificación: " + clientNumberid));

        Long activeStatusId = masterDataRepositoryPort.getIdByCode("ACT");
        if (activeStatusId == null) {
            throw new IllegalStateException("Estado Activo ('ACT') no encontrado en MasterData");
        }

        CreditAccountDomain account = creditRepositoryPort.findByClientIdAndOutletId(client.getId(), outletId)
                .orElseGet(() -> {
                    // Auto-initialize credit account for trusted neighbor
                    CreditAccountDomain newAcc = CreditAccountDomain.create(
                            client.getId(),
                            outletId,
                            DEFAULT_CREDIT_LIMIT,
                            activeStatusId
                    );
                    return creditRepositoryPort.save(newAcc);
                });

        return mapToResponse(account, client);
    }

    /**
     * Retrieves the audit ledger ("libreta") transactions for a client.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CreditTransactionResponse> findTransactions(String clientNumberid, Long outletId) {
        PersonDomain client = personRepositoryPort.findByNumberid(clientNumberid)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con identificación: " + clientNumberid));

        Optional<CreditAccountDomain> accountOpt = creditRepositoryPort.findByClientIdAndOutletId(client.getId(), outletId);
        if (accountOpt.isEmpty()) return new ArrayList<>();

        List<CreditTransactionResponse> responses = new ArrayList<>();
        creditRepositoryPort.findTransactionsByAccountId(accountOpt.get().getId()).forEach(txn -> {
            String employeeName = personRepositoryPort.findById(txn.getRegisteredBy())
                    .map(PersonDomain::getFullName)
                    .orElse("Sistema / Empleado");

            responses.add(new CreditTransactionResponse(
                    txn.getId(),
                    txn.getCreditAccountId(),
                    txn.getSaleId(),
                    txn.getType(),
                    txn.getAmount(),
                    txn.getPreviousDebt(),
                    txn.getNewDebt(),
                    txn.getNotes(),
                    txn.getRegisteredBy(),
                    employeeName,
                    txn.getCreatedAt()
            ));
        });

        return responses;
    }

    /**
     * Lists all credit accounts (debtors) active in a specific store.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CreditAccountResponse> findAllByStore(Long outletId) {
        List<CreditAccountResponse> responses = new ArrayList<>();
        creditRepositoryPort.findAllByOutletId(outletId).forEach(account -> {
            PersonDomain client = personRepositoryPort.findById(account.getClientId())
                    .orElse(null);
            if (client != null) {
                responses.add(mapToResponse(account, client));
            }
        });
        return responses;
    }

    private CreditAccountResponse mapToResponse(CreditAccountDomain account, PersonDomain client) {
        return new CreditAccountResponse(
                account.getId(),
                account.getClientId(),
                client.getFullName(),
                client.getNumberid(),
                account.getOutletId(),
                account.getCreditLimit(),
                account.getCurrentDebt(),
                "ACTIVO",
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
