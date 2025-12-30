package org.frias.avalon.maestra.services.interfaces;

import org.frias.avalon.maestra.entities.MasterData;

public interface MasterDataSalesService {

    MasterData findById(Long id);
    MasterData searchShortName(String shortName);
}
