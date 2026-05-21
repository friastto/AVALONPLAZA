package org.frias.avalon.domain.user.application.usecase.accesrefreshtoken;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.application.service.BuildAuthenticationResponse;
import org.frias.avalon.domain.user.domain.model.RefreshTokenDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RefreshTokenRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GenerateAccessTokenAndRefreshTokenUseCaseImpl implements GenerateAccessTokenAndRefreshTokenUseCase {
    private final RefreshTokenRepositoryPort refreshTokenPort;
    private final UserDetailsService userDetailsService;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final BuildAuthenticationResponse buildAuthenticationResponse;


    @Override
    public AuthResponse execute(String refreshToken) {


        // 1. VALIDACIÓN: ¿El refreshToken existe en la base de datos?
        RefreshTokenDomain oldRefreshToken = refreshTokenPort.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("El Refresh Token proporcionado no es válido."));

        // 2. VALIDACIÓN: ¿El refresh token ya fue revocado previamente? (Control de accesos concurrentes/fraude)
        if (oldRefreshToken.isRevoked()) {
            // Mitigación de riesgos: Si un token revocado vuelve a presentarse, podría ser un ataque
            throw new RuntimeException("Este Refresh Token ya ha sido revocado. Acceso denegado.");
        }

        // 3. VALIDACIÓN: ¿El token ya expiró por tiempo?
        if (oldRefreshToken.isExpired()) {
            // Regla limpia: Si ya no sirve, lo barremos de la persistencia para no acumular basura
            refreshTokenPort.deleteByRefreshToken(refreshToken);
            throw new RuntimeException("El Refresh Token ha expirado. Por favor, inicie sesión nuevamente.");
        }

        UserAvalonDomain userAvalonDomain = userAvalonRepositoryPort.findById(oldRefreshToken.getUserAvalonId())
                .orElseThrow(() -> new EntityNotFoundException("usuario no encontrado para el rftokn "));

        UserDetails userDetails = userDetailsService.loadUserByUsername(userAvalonDomain.getUserName());

        if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
            throw new IllegalStateException("La cuenta del usuario no está activa o está bloqueada.");
        }

        // -------------------------------------------------------------------------
        // SI TODAS LAS VALIDACIONES PASAN: Iniciamos el proceso de renovación limpia
        // -------------------------------------------------------------------------

        // 4. APLICAR ROTACIÓN (Máxima Seguridad): Revocamos el token actual inmediatamente
        oldRefreshToken.revoke();
        refreshTokenPort.save(oldRefreshToken);

        return buildAuthenticationResponse.buildAuthenticationResponse(userAvalonDomain, userDetails);
    }
}