package org.frias.avalon.domain.masterdata.application.usecase.find;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;

import java.util.List;

public interface FindAllMasterDataUseCase {

    List<MasterDataResponseDto> execute();
}
