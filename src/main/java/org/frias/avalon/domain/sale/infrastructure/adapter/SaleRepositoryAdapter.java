package org.frias.avalon.domain.sale.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.sale.infrastructure.entity.SaleEntity;
import org.frias.avalon.domain.sale.infrastructure.mapper.SaleMapper;
import org.frias.avalon.domain.sale.infrastructure.repository.JpaSaleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SaleRepositoryAdapter implements SaleRepositoryPort {

    private final JpaSaleRepository jpaSaleRepository;
    private final SaleMapper saleMapper;

    @Override
    public SaleDomain save(SaleDomain sale) {
        SaleEntity entity = saleMapper.toEntity(sale);
        SaleEntity saved = jpaSaleRepository.save(entity);
        return saleMapper.toDomain(saved);
    }

    @Override
    public Optional<SaleDomain> findByCode(UUID code) {
        return jpaSaleRepository.findBySaleCode(code)
                .map(saleMapper::toDomain);
    }

    @Override
    public Page<SaleDomain> findByOutletId(Long outletId, Pageable pageable) {
        return jpaSaleRepository.findByOutletId(outletId, pageable)
                .map(saleMapper::toDomain);
    }
}
