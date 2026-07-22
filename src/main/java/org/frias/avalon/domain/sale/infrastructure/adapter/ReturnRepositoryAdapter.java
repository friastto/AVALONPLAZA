package org.frias.avalon.domain.sale.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.sale.application.port.ReturnRepositoryPort;
import org.frias.avalon.domain.sale.domain.ReturnDomain;
import org.frias.avalon.domain.sale.infrastructure.entity.ReturnEntity;
import org.frias.avalon.domain.sale.infrastructure.mapper.ReturnMapper;
import org.frias.avalon.domain.sale.infrastructure.repository.JpaReturnRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReturnRepositoryAdapter implements ReturnRepositoryPort {

    private final JpaReturnRepository jpaReturnRepository;
    private final ReturnMapper returnMapper;

    @Override
    public ReturnDomain save(ReturnDomain returnDomain) {
        ReturnEntity entity = returnMapper.toEntity(returnDomain);
        ReturnEntity saved = jpaReturnRepository.save(entity);
        return returnMapper.toDomain(saved);
    }

    @Override
    public Optional<ReturnDomain> findByCode(UUID returnCode) {
        return jpaReturnRepository.findByReturnCode(returnCode)
                .map(returnMapper::toDomain);
    }

    @Override
    public List<ReturnDomain> findByOriginalSaleId(Long originalSaleId) {
        return jpaReturnRepository.findByOriginalSaleId(originalSaleId)
                .stream().map(returnMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Page<ReturnDomain> findByOutletId(Long outletId, Pageable pageable) {
        return jpaReturnRepository.findByOutletId(outletId, pageable)
                .map(returnMapper::toDomain);
    }
}
