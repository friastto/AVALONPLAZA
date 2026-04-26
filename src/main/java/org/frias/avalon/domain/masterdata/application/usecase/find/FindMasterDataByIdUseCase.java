package org.frias.avalon.domain.masterdata.application.usecase.find;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;

public interface FindMasterDataByIdUseCase {

    MasterDataResponseDto execute(Long id);


}
