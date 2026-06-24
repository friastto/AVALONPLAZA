package org.frias.avalon.domain.masterdata.application.usecase.delete;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;

public interface DeleteMasterDataUseCase {
    MasterDataResponseDto execute(Long id);
}