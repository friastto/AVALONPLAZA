package org.frias.avalon.domain.user.domain.port;

import org.frias.avalon.domain.user.domain.model.PasswordResetTokenDomain;
import java.util.Optional;

public interface PasswordResetTokenRepositoryPort {
    PasswordResetTokenDomain save(PasswordResetTokenDomain tokenDomain);
    Optional<PasswordResetTokenDomain> findByVerificationToken(String verificationToken);
    Optional<PasswordResetTokenDomain> findByUserIdAndPin(Long userId, String pin);
    void delete(PasswordResetTokenDomain tokenDomain);
}