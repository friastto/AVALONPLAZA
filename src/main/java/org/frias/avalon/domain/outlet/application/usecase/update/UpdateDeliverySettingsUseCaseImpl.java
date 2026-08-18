package org.frias.avalon.domain.outlet.application.usecase.update;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.outlet.application.dto.request.UpdateDeliverySettingsRequestDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.outlet.infraestructure.mapper.OutletMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateDeliverySettingsUseCaseImpl implements UpdateDeliverySettingsUseCase {

    private final OutletRepositoryPort outletRepositoryPort;
    private final OutletMapper outletMapper;

    @Override
    public OutletResponseDto execute(Long outletId, UpdateDeliverySettingsRequestDto request) {
        OutletDomain domain = outletRepositoryPort.findById(outletId)
                .orElseThrow(() -> new ResourceNotFoundException("Outlet no encontrado con id: " + outletId));

        if (request.deliveryEnabled() != null) {
            domain.setDeliveryEnabled(request.deliveryEnabled());
        }
        if (request.deliveryFee() != null) {
            domain.setDeliveryFee(request.deliveryFee());
        }

        OutletDomain updated = outletRepositoryPort.update(domain);
        return outletMapper.toResponse(updated);
    }
}
