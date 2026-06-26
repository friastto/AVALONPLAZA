package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.domain.outlet.application.dto.request.OutletSearchCriteria;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindAllOutletsUseCase {
    Page<OutletResponseDto> execute(OutletSearchCriteria criteria, Pageable pageable);
}
