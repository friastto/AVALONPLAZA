package org.frias.avalon.maestra.services.interfaces;

import org.frias.avalon.maestra.entities.MasterData;

public interface MasterDataProductService {

    MasterData findClientByNameShort(String nameShort);
    MasterData findById(Long id);

}
