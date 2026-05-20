package org.frias.avalon.domain.user.application.usecase.accesrefreshtoken;

import org.frias.avalon.core.jwt.service.JwtTokenProviderPort;
import org.frias.avalon.domain.user.application.dtos.response.TokenRefreshResult;
import org.frias.avalon.domain.user.domain.model.RefreshTokenDomain;
import org.frias.avalon.domain.user.domain.port.RefreshTokenRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AccessRefreshTokenUseCaseImpl implements AccessRefreshTokenUseCase {
    private final RefreshTokenRepositoryPort refreshTokenPort;
    private final JwtTokenProviderPort tokenProviderPort;

    public AccessRefreshTokenUseCaseImpl(RefreshTokenRepositoryPort refreshTokenPort, JwtTokenProviderPort tokenProviderPort) {
        this.refreshTokenPort = refreshTokenPort;
        this.tokenProviderPort = tokenProviderPort;
    }

    @Override
    public TokenRefreshResult execute(String requestToken) {

        // 1. VALIDACIÓN: ¿El token existe en la base de datos?
        RefreshTokenDomain oldRefreshToken = refreshTokenPort.findByToken(requestToken)
                .orElseThrow(() -> new RuntimeException("El Refresh Token proporcionado no es válido."));

        // 2. VALIDACIÓN: ¿El token ya fue revocado previamente? (Control de accesos concurrentes/fraude)
        if (oldRefreshToken.isRevoked()) {
            // Mitigación de riesgos: Si un token revocado vuelve a presentarse, podría ser un ataque
            throw new RuntimeException("Este Refresh Token ya ha sido revocado. Acceso denegado.");
        }

        // 3. VALIDACIÓN: ¿El token ya expiró por tiempo?
        if (oldRefreshToken.isExpired()) {
            // Regla limpia: Si ya no sirve, lo barremos de la persistencia para no acumular basura
            refreshTokenPort.deleteByToken(requestToken);
            throw new RuntimeException("El Refresh Token ha expirado. Por favor, inicie sesión nuevamente.");
        }

        // -------------------------------------------------------------------------
        // SI TODAS LAS VALIDACIONES PASAN: Iniciamos el proceso de renovación limpia
        // -------------------------------------------------------------------------

        // 4. APLICAR ROTACIÓN (Máxima Seguridad): Revocamos el token actual inmediatamente
        oldRefreshToken.revoke();
        refreshTokenPort.save(oldRefreshToken);

        // 5. GENERAR NUEVO ACCESS TOKEN (JWT)
        // Usamos el id del usuario asignado que recuperamos limpiamente del dominio, inyectando el puerto de JWT
        String newAccessToken = tokenProviderPort.generateAccessTokenFromId(oldRefreshToken.getUserAvalonId());

        // 6. GENERAR UN NUEVO REFRESH TOKEN EN DOMINIO
        // Generamos una cadena aleatoria criptográficamente segura para el cuerpo del token
        String newRefreshTokenStr = UUID.randomUUID().toString();
        Long refreshExpirationMs = 86400000L;
        Instant newExpiryDate = Instant.now().plusMillis(refreshExpirationMs);

        // Usamos tu constructor de negocio nativo que inicializa el UUID del token, el issuedAt y pone revoked en false
        RefreshTokenDomain newRefreshToken = new RefreshTokenDomain(
                newRefreshTokenStr,
                oldRefreshToken.getUserAvalonId(),
                newExpiryDate
        );

        // 7. PERSISTIR EL NUEVO REFRESH TOKEN
        refreshTokenPort.save(newRefreshToken);

        // 8. RETORNAR EL RESULTADO DESACOPLADO
        return new TokenRefreshResult(newAccessToken, newRefreshTokenStr);
    }
}