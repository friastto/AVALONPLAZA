package org.frias.avalon.domain.masterdata.services.interfaces;

import org.frias.avalon.domain.masterdata.entities.MasterData;

public interface MasterDataProductService {

    MasterData searchByNameShortAndStatusActive(String nameShort);
    MasterData searchById(Long id);

}
