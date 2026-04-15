package org.frias.avalon.domain.company.application.usecase.inter.company;

import org.frias.avalon.domain.outlet.dtos.request.OutletNewDto;
import org.frias.avalon.domain.outlet.dtos.response.OutletDto;

public interface CreateOutletUseCase {

    OutletDto execute(OutletNewDto request);
}
