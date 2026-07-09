package org.frias.avalon.domain.credit.application.port;

import org.frias.avalon.domain.credit.domain.model.CreditAccountDomain;
import org.frias.avalon.domain.credit.domain.model.CreditTransactionDomain;
import java.util.List;
import java.util.Optional;

/**
 * Output Port (interface) for persisting credit accounts and transaction ledgers.
 * Follows Hexagonal Architecture principles.
 */
public interface CreditRepositoryPort {

    /**
     * Saves or updates a credit account.
     *
     * @param account The credit account model.
     * @return The saved credit account model.
     */
    CreditAccountDomain save(CreditAccountDomain account);

    /**
     * Saves a credit transaction ledger entry.
     *
     * @param transaction The credit transaction model.
     * @return The saved credit transaction model.
     */
    CreditTransactionDomain save(CreditTransactionDomain transaction);

    /**
     * Finds a client's credit account in a specific outlet/store.
     *
     * @param clientId The ID of the client (Person).
     * @param outletId The ID of the store.
     * @return An optional containing the credit account if found.
     */
    Optional<CreditAccountDomain> findByClientIdAndOutletId(Long clientId, Long outletId);

    /**
     * Finds a credit account by its ID.
     *
     * @param id The account ID.
     * @return An optional containing the account if found.
     */
    Optional<CreditAccountDomain> findById(Long id);

    /**
     * Finds all credit transactions associated with an account.
     *
     * @param creditAccountId The ID of the credit account.
     * @return A list of transactions.
     */
    List<CreditTransactionDomain> findTransactionsByAccountId(Long creditAccountId);

    /**
     * Finds all credit accounts registered for a specific store.
     *
     * @param outletId The ID of the store.
     * @return A list of credit accounts.
     */
    List<CreditAccountDomain> findAllByOutletId(Long outletId);
}
