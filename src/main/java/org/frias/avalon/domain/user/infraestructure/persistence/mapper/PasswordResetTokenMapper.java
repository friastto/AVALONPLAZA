package org.frias.avalon.domain.user.infraestructure.persistence.mapper;

import org.frias.avalon.domain.user.domain.model.PasswordResetTokenDomain;
import org.frias.avalon.domain.user.infraestructure.persistence.entity.PasswordResetToken;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenMapper {

    public PasswordResetTokenDomain toDomain(PasswordResetToken entity) {
        if (entity == null) {
            return null;
        }
        return new PasswordResetTokenDomain(
                entity.getId(),
                entity.getPin(),
                entity.getVerificationToken(),
                entity.getUserId(),
                entity.getExpiryDate()
        );
    }

    public PasswordResetToken toEntity(PasswordResetTokenDomain domain) {
        if (domain == null) {
            return null;
        }
        return new PasswordResetToken(
                domain.getId(),
                domain.getPin(),
                domain.getVerificationToken(),
                domain.getUserId(),
                domain.getExpiryDate()
        );
    }
}