package org.frias.avalon.domain.user.infraestructure.persistence.repository;

import org.frias.avalon.domain.user.infraestructure.persistence.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaPasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByVerificationToken(String verificationToken);
    Optional<PasswordResetToken> findByUserIdAndPin(Long userId, String pin);
}