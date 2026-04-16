package org.frias.avalon.domain.user.application.usecase.impl.publics;

import org.frias.avalon.domain.user.application.authservices.interfaces.AuthService;
import org.frias.avalon.domain.user.application.usecase.inter.publics.AuthloginUseCase;
import org.frias.avalon.domain.user.domain.dtos.request.AuthRequest;
import org.frias.avalon.domain.user.domain.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.domain.dtos.response.UserResponseDto;
import org.springframework.stereotype.Service;

@Service
public class AuthloginUseCaseImpl implements AuthloginUseCase {

    private final AuthService authService;

    public AuthloginUseCaseImpl(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public AuthResponse execute(AuthRequest request) {

        return execute(request.username(),request.password());
    }

    public AuthResponse execute(String userName, String password){

        return authService.login(userName, password);
    }


}
