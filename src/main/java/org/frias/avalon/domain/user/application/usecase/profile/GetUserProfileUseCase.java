package org.frias.avalon.domain.user.application.usecase.profile;

import org.frias.avalon.domain.user.application.dtos.response.UserProfileResponseDto;

public interface GetUserProfileUseCase {

    UserProfileResponseDto execute(String identifier);

    UserProfileResponseDto executeCurrent();
}
