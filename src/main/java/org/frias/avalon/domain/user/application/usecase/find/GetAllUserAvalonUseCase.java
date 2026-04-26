package org.frias.avalon.domain.user.application.usecase.find;

import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;

import java.util.List;

public interface GetAllUserAvalonUseCase {
    List<UserAvalonResponseDto> execute();
}
