package org.frias.avalon.domain.company.application.usecase.inter.company;

import org.frias.avalon.domain.outlet.dtos.response.OutletDto;

import java.util.List;

public interface ShowAllOutletsUseCase {
    List<OutletDto> execute();
}
