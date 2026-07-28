package org.frias.avalon.domain.cashregister.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.cashregister.application.port.CashSessionRepositoryPort;
import org.frias.avalon.domain.cashregister.domain.CashExpenseDomain;
import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;
import org.frias.avalon.domain.cashregister.infrastructure.entity.CashExpenseEntity;
import org.frias.avalon.domain.cashregister.infrastructure.entity.CashSessionEntity;
import org.frias.avalon.domain.cashregister.infrastructure.mapper.CashExpenseMapper;
import org.frias.avalon.domain.cashregister.infrastructure.mapper.CashSessionMapper;
import org.frias.avalon.domain.cashregister.infrastructure.repository.JpaCashExpenseRepository;
import org.frias.avalon.domain.cashregister.infrastructure.repository.JpaCashSessionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CashSessionRepositoryAdapter implements CashSessionRepositoryPort {

    private final JpaCashSessionRepository jpaCashSessionRepository;
    private final JpaCashExpenseRepository jpaCashExpenseRepository;
    private final org.frias.avalon.domain.cashregister.infrastructure.repository.JpaCashPickupRepository jpaCashPickupRepository;
    private final CashSessionMapper cashSessionMapper;
    private final CashExpenseMapper cashExpenseMapper;

    @Override
    public CashSessionDomain saveSession(CashSessionDomain session) {
        CashSessionEntity entity = cashSessionMapper.toEntity(session);
        CashSessionEntity saved = jpaCashSessionRepository.save(entity);
        return cashSessionMapper.toDomain(saved);
    }

    @Override
    public Optional<CashSessionDomain> findSessionById(Long id) {
        return jpaCashSessionRepository.findById(id)
                .map(cashSessionMapper::toDomain);
    }

    @Override
    public Optional<CashSessionDomain> findActiveSession(Long outletId, Long employeeId) {
        Optional<CashSessionDomain> session = jpaCashSessionRepository.findByOutletIdAndEmployeeIdAndStatus(outletId, employeeId, "OPEN")
                .map(cashSessionMapper::toDomain);
        if (session.isPresent()) {
            return session;
        }
        // Fallback: Si existe una sesión activa abierta en esta tienda, resolverla para el empleado
        return jpaCashSessionRepository.findByOutletIdAndStatus(outletId, "OPEN").stream()
                .findFirst()
                .map(cashSessionMapper::toDomain);
    }

    @Override
    public List<CashSessionDomain> findActiveSessionsByOutlet(Long outletId) {
        return jpaCashSessionRepository.findByOutletIdAndStatus(outletId, "OPEN").stream()
                .map(cashSessionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CashSessionDomain> findAllSessionsByOutlet(Long outletId) {
        return jpaCashSessionRepository.findByOutletIdOrderByOpenedAtDesc(outletId).stream()
                .map(cashSessionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public CashExpenseDomain saveExpense(CashExpenseDomain expense) {
        CashExpenseEntity entity = cashExpenseMapper.toEntity(expense);
        CashExpenseEntity saved = jpaCashExpenseRepository.save(entity);
        return cashExpenseMapper.toDomain(saved);
    }

    @Override
    public List<CashExpenseDomain> findExpensesBySessionId(Long cashSessionId) {
        return jpaCashExpenseRepository.findByCashSessionId(cashSessionId).stream()
                .map(cashExpenseMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CashExpenseDomain> findExpensesBySessionIds(List<Long> cashSessionIds) {
        if (cashSessionIds == null || cashSessionIds.isEmpty()) {
            return List.of();
        }
        return jpaCashExpenseRepository.findByCashSessionIdIn(cashSessionIds).stream()
                .map(cashExpenseMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public org.frias.avalon.domain.cashregister.domain.CashPickupDomain savePickup(org.frias.avalon.domain.cashregister.domain.CashPickupDomain pickup) {
        org.frias.avalon.domain.cashregister.infrastructure.entity.CashPickupEntity entity = cashSessionMapper.toPickupEntity(pickup);
        org.frias.avalon.domain.cashregister.infrastructure.entity.CashPickupEntity saved = jpaCashPickupRepository.save(entity);
        return cashSessionMapper.toPickupDomain(saved);
    }

    @Override
    public List<org.frias.avalon.domain.cashregister.domain.CashPickupDomain> findPickupsBySessionId(Long cashSessionId) {
        return jpaCashPickupRepository.findBySessionId(cashSessionId).stream()
                .map(cashSessionMapper::toPickupDomain)
                .collect(Collectors.toList());
    }
}
