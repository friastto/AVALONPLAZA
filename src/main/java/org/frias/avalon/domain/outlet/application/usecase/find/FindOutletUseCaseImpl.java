package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.domain.outlet.application.dto.request.FindOutletRequestDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.outlet.infraestructure.mapper.OutletMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class FindOutletUseCaseImpl implements FindOutletUseCase {

    private final OutletRepositoryPort outletPort;
    private final OutletMapper outletMapper;

    public FindOutletUseCaseImpl(OutletRepositoryPort outletPort, OutletMapper outletMapper) {
        this.outletPort = outletPort;
        this.outletMapper = outletMapper;
    }

    @Override
    public List<OutletResponseDto> execute(FindOutletRequestDto request) {
        if (request.nit() != null && !request.nit().isBlank()) {
            Optional<OutletDomain> outletDomainOptional = outletPort.findByNit(request.nit());
            return outletDomainOptional.map(outletMapper::toResponse).map(List::of).orElse(Collections.emptyList());
        }
        // Aquí se podría implementar la búsqueda por otros criterios, como el nombre.
        // Por ahora, si no hay NIT, devolvemos una lista vacía.
        return Collections.emptyList();
    }
}