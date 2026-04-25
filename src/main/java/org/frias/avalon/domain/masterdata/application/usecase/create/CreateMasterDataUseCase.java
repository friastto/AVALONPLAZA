package org.frias.avalon.domain.masterdata.application.usecase.create;

import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataNewDto;

public interface CreateMasterDataUseCase {
    Long execute(MasterDataNewDto request);
}
