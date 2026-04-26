package org.frias.avalon.domain.masterdata.application.usecase.create;

import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;

import java.util.List;

public interface CreateAllMasterDataUseCase {
    List<MasterDataResponseDto> execute(List<MasterDataNewDto> request);
}
