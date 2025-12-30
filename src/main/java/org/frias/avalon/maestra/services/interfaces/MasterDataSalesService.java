package org.frias.avalon.maestra.services.interfaces;

import org.frias.avalon.maestra.dtos.MasterDataRequestCreateDto;
import org.frias.avalon.maestra.dtos.MasterDataResponseDto;
import org.frias.avalon.maestra.entities.MasterData;

import java.util.List;

public interface MasterDataSalesService {

    MasterData findById(Long id);
    MasterData searchShortName(String shortName);
}
