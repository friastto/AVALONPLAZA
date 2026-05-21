package org.frias.avalon.domain.user.application.usecase.login;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.frias.avalon.core.jwt.config.CustomUserDetailsService;
import org.frias.avalon.core.validation.PassSecure;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.user.application.dtos.request.AuthRequest;
import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.application.service.BuildAuthenticationResponse;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LoginUseCaseImpl implements LoginUseCase {

    private final UserAvalonRepositoryPort userPort;
    private final MasterTreeProvider masterTreeProvider;
    private final CustomUserDetailsService userDetailsService;
    private final BuildAuthenticationResponse buildAuthenticationResponse;


    @Override
    public AuthResponse execute(AuthRequest request) {

        UserAvalonDomain user = userPort.findByIdentifier(request.identifier())
                .orElseThrow(() -> new EntityNotFoundException("usuario no encontrado"));

        var tree = masterTreeProvider.getTree();

        MasterRoot status = tree.getById(user.getStatusId());

        if (status == null) {
            throw new IllegalStateException("Estado inconsistente en cache");
        }

        if (!PassSecure.verifyPassword(request.password(), user.getHashSalt(), user.getHashPassword()))
            throw new IllegalStateException("Credenciales inválidas");

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserName());

        if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
            throw new IllegalStateException("La cuenta del usuario no está activa o está bloqueada.");
        }

        //se delega la creacion de los permisos y la respuesta al servicio de creacion de el UTHRESPONSE
        return buildAuthenticationResponse.buildAuthenticationResponse(user, userDetails);
    }
}
