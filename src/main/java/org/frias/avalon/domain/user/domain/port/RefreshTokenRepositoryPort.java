package org.frias.avalon.domain.user.domain.port;

import org.frias.avalon.domain.user.domain.model.RefreshTokenDomain;
import org.frias.avalon.domain.user.infraestructure.persistence.entity.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {
    RefreshTokenDomain save(RefreshTokenDomain refreshToken) ;

    Optional<RefreshTokenDomain> findByToken(String token) ;

    void deleteByUser(Long userAvalonId) ;

   void delete(UUID refreshToken) ;

   void deleteByToken(String token);


}
