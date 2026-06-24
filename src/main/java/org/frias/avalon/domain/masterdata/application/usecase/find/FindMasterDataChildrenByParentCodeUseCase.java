package org.frias.avalon.domain.masterdata.application.usecase.find;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;

import java.util.List;

public interface FindMasterDataChildrenByParentCodeUseCase {
    List<MasterDataResponseDto> execute(String parentCode);
}