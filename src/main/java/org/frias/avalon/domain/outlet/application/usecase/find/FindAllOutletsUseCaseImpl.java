package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.domain.outlet.application.dto.request.OutletSearchCriteria;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.outlet.infraestructure.mapper.OutletMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FindAllOutletsUseCaseImpl implements FindAllOutletsUseCase {

    private final OutletRepositoryPort outletPort;
    private final OutletMapper outletMapper;

    public FindAllOutletsUseCaseImpl(OutletRepositoryPort outletPort, OutletMapper outletMapper) {
        this.outletPort = outletPort;
        this.outletMapper = outletMapper;
    }

    @Override
    public Page<OutletResponseDto> execute(OutletSearchCriteria criteria, Pageable pageable) {
        return outletPort.findAll(criteria, pageable)
                .map(outletMapper::toResponse);
    }
}
