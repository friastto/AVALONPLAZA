package org.frias.avalon.domain.user.application.usecase.find;

import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;

public interface FindByPersonNumberIdUseCase {
    UserAvalonResponseDto execute(String numnerId);
}
