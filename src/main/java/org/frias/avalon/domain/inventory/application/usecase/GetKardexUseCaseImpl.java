package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.domain.inventory.application.dto.KardexResponseDto;
import org.frias.avalon.domain.inventory.infrastructure.entity.StockMovementEntity;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaStockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of GetKardexUseCase Input Port.
 */
@Service
public class GetKardexUseCaseImpl implements GetKardexUseCase {

    private final JpaStockMovementRepository stockMovementRepository;

    public GetKardexUseCaseImpl(JpaStockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<KardexResponseDto> findByProductOutletId(Long productOutletId) {
        return stockMovementRepository.findByProductOutletIdOrderByCreatedAtDesc(productOutletId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<KardexResponseDto> findByOutletId(Long outletId) {
        return stockMovementRepository.findByOutletIdOrderByCreatedAtDesc(outletId).stream()
                .map(this::toDto)
                .toList();
    }

    private KardexResponseDto toDto(StockMovementEntity entity) {
        return new KardexResponseDto(
                entity.getId(),
                entity.getProductOutletId(),
                entity.getOutletId(),
                entity.getMovementType(),
                entity.getQuantityBefore(),
                entity.getQuantityAfter(),
                entity.getQuantityDelta(),
                entity.getReason(),
                entity.getOperatorId(),
                entity.getCreatedAt()
        );
    }
}
