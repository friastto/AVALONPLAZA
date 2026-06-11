package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.domain.outlet.application.dto.request.FindOutletRequestDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;

import java.util.List;

public interface FindOutletUseCase {
    List<OutletResponseDto> execute(FindOutletRequestDto request);
}