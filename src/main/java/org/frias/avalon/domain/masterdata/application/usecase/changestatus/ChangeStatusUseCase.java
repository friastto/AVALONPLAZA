package org.frias.avalon.domain.masterdata.application.usecase.changestatus;

import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataUpdateStatusDto;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;

public interface ChangeStatusUseCase {

    MasterDataResponseDto execute(MasterDataUpdateStatusDto dataDto);
}
