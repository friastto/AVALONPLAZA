package org.frias.avalon.domain.user.domain.port;

import org.frias.avalon.domain.user.domain.model.RefreshTokenDomain;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {
    RefreshTokenDomain save(RefreshTokenDomain refreshToken);

    Optional<RefreshTokenDomain> findByRefreshToken(String refreshTokenValue);

    void deleteByUser(Long userAvalonId);

    void delete(UUID id);

    void deleteByRefreshToken(String refreshTokenValue);
}
