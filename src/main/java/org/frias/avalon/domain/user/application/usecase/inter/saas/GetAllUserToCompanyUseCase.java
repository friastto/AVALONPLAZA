package org.frias.avalon.domain.user.application.usecase.inter.saas;

import org.frias.avalon.domain.user.domain.dtos.response.UserAvalonDto;
import org.frias.avalon.domain.user.domain.dtos.response.UserResponseDto;

import java.util.List;

public interface GetAllUserToCompanyUseCase {

    List<UserAvalonDto> execute(Long idCompany);




}
