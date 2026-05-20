package org.frias.avalon.domain.user.infraestructure.persistence.repository;

import jakarta.transaction.Transactional;
import org.frias.avalon.domain.user.infraestructure.persistence.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    @Modifying
    @Transactional
    void deleteByUserAvalonId( Long userAvalonId);

    @Modifying
    @Transactional
    void deleteByRefreshToken(String refreshToken);
}