package org.frias.avalon.domain.outlet.application.usecase.create;

import org.frias.avalon.domain.outlet.application.dto.request.OutletCreateRequestDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;

public interface CreateOutletUseCase {

    OutletResponseDto execute(OutletCreateRequestDto dto);
}
