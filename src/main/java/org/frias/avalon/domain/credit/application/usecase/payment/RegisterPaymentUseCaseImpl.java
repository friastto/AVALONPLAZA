package org.frias.avalon.domain.credit.application.usecase.payment;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.credit.application.dto.request.RegisterPaymentRequest;
import org.frias.avalon.domain.credit.application.dto.response.CreditTransactionResponse;
import org.frias.avalon.domain.credit.application.port.CreditRepositoryPort;
import org.frias.avalon.domain.credit.domain.model.CreditAccountDomain;
import org.frias.avalon.domain.credit.domain.model.CreditTransactionDomain;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

/**
 * Case of use implementation to process payments/installments ("abonos") on client credit accounts.
 */
@Service
@RequiredArgsConstructor
public class RegisterPaymentUseCaseImpl implements RegisterPaymentUseCase {

    private final CreditRepositoryPort creditRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final CurrentUserProviderPort currentUserProvider;

    /**
     * Records a payment to reduce client outstanding debt, creating a transaction log.
     *
     * @param request The payment details request.
     * @return The transaction response DTO auditing the payment.
     * @throws ResourceNotFoundException If the client or store credit account is not found.
     */
    @Override
    @Transactional
    public CreditTransactionResponse execute(RegisterPaymentRequest request) {
        PersonDomain client = personRepositoryPort.findByNumberid(request.clientNumberid())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con identificación: " + request.clientNumberid()));

        CreditAccountDomain account = creditRepositoryPort.findByClientIdAndOutletId(client.getId(), request.outletId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró cuenta de crédito para el cliente en este establecimiento"));

        // Get registering employee
        UserContext userContext = currentUserProvider.getCurrentUserContext();
        UserAvalonDomain employeeUser = userAvalonRepositoryPort.findByUserName(userContext.username())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario empleado actual no encontrado"));
        Long employeeId = employeeUser.getPersonId();

        PersonDomain employeePerson = personRepositoryPort.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha de persona del empleado no encontrada"));

        BigDecimal oldDebt = account.getCurrentDebt();
        account.pay(request.amount());
        BigDecimal newDebt = account.getCurrentDebt();

        // Save account balance update
        creditRepositoryPort.save(account);

        // Record Libreta transaction
        String notes = request.notes() != null && !request.notes().isBlank() 
                ? request.notes() 
                : "Abono a deuda de fiado";

        CreditTransactionDomain txn = CreditTransactionDomain.create(
                account.getId(),
                null,
                "PAYMENT",
                request.amount(),
                oldDebt,
                newDebt,
                notes,
                employeeId
        );

        CreditTransactionDomain savedTxn = creditRepositoryPort.save(txn);

        return new CreditTransactionResponse(
                savedTxn.getId(),
                savedTxn.getCreditAccountId(),
                null,
                savedTxn.getType(),
                savedTxn.getAmount(),
                savedTxn.getPreviousDebt(),
                savedTxn.getNewDebt(),
                savedTxn.getNotes(),
                savedTxn.getRegisteredBy(),
                employeePerson.getFullName(),
                savedTxn.getCreatedAt()
        );
    }
}
