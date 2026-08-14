package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.outlet.infraestructure.mapper.OutletMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of FindOutletsByCompanyUseCase.
 */
@Service
public class FindOutletsByCompanyUseCaseImpl implements FindOutletsByCompanyUseCase {

    private final OutletRepositoryPort outletPort;
    private final OutletMapper outletMapper;

    public FindOutletsByCompanyUseCaseImpl(OutletRepositoryPort outletPort, OutletMapper outletMapper) {
        this.outletPort = outletPort;
        this.outletMapper = outletMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public List<OutletResponseDto> execute(Long companyId) {
        return outletPort.findByCompanyId(companyId).stream()
                .map(outletMapper::toResponse)
                .toList();
    }
}
