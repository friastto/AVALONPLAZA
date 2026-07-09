package org.frias.avalon.domain.credit.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.credit.application.port.CreditRepositoryPort;
import org.frias.avalon.domain.credit.domain.model.CreditAccountDomain;
import org.frias.avalon.domain.credit.domain.model.CreditTransactionDomain;
import org.frias.avalon.domain.credit.infrastructure.entity.CreditAccountEntity;
import org.frias.avalon.domain.credit.infrastructure.entity.CreditTransactionEntity;
import org.frias.avalon.domain.credit.infrastructure.mapper.CreditMapper;
import org.frias.avalon.domain.credit.infrastructure.repository.JpaCreditAccountRepository;
import org.frias.avalon.domain.credit.infrastructure.repository.JpaCreditTransactionRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CreditRepositoryAdapter implements CreditRepositoryPort {

    private final JpaCreditAccountRepository accountRepository;
    private final JpaCreditTransactionRepository transactionRepository;
    private final CreditMapper mapper;

    @Override
    public CreditAccountDomain save(CreditAccountDomain account) {
        CreditAccountEntity entity = mapper.toEntity(account);
        CreditAccountEntity saved = accountRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public CreditTransactionDomain save(CreditTransactionDomain transaction) {
        CreditTransactionEntity entity = mapper.toEntity(transaction);
        CreditTransactionEntity saved = transactionRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CreditAccountDomain> findByClientIdAndOutletId(Long clientId, Long outletId) {
        return accountRepository.findByClientIdAndOutletId(clientId, outletId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<CreditAccountDomain> findById(Long id) {
        return accountRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<CreditTransactionDomain> findTransactionsByAccountId(Long creditAccountId) {
        return transactionRepository.findAllByCreditAccountIdOrderByCreatedAtDesc(creditAccountId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CreditAccountDomain> findAllByOutletId(Long outletId) {
        return accountRepository.findAllByOutletId(outletId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
