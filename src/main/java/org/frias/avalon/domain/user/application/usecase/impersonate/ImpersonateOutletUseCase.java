package org.frias.avalon.domain.user.application.usecase.impersonate;

import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;

@Deprecated
public interface ImpersonateOutletUseCase {
    AuthResponse execute(Long outletId);
}
