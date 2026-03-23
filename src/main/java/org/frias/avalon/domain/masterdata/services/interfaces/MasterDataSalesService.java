package org.frias.avalon.domain.masterdata.services.interfaces;

import org.frias.avalon.domain.masterdata.entities.MasterData;

public interface MasterDataSalesService {

    MasterData searchById(Long id);

    MasterData searchByShortName(String shortName);
}
