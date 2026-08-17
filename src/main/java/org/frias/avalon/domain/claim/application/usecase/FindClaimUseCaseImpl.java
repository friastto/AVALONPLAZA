package org.frias.avalon.domain.claim.application.usecase;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.claim.application.dto.response.ClaimResponse;
import org.frias.avalon.domain.claim.application.port.ClaimRepositoryPort;
import org.frias.avalon.domain.claim.infrastructure.persistence.mapper.ClaimMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FindClaimUseCaseImpl implements FindClaimUseCase {

    private final ClaimRepositoryPort claimRepositoryPort;
    private final ClaimMapper claimMapper;

    @Override
    @Transactional(readOnly = true)
    public ClaimResponse findById(Long id) {
        return claimRepositoryPort.findById(id)
                .map(claimMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Reclamo con ID " + id + " no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimResponse> findByOrderId(Long orderId) {
        return claimRepositoryPort.findAllByOrderId(orderId).stream()
                .map(claimMapper::toResponse)
                .collect(Collectors.toList());
    }
}
