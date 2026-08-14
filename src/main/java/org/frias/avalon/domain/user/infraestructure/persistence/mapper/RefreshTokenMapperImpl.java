package org.frias.avalon.domain.user.infraestructure.persistence.mapper;

import org.frias.avalon.domain.user.domain.model.RefreshTokenDomain;
import org.frias.avalon.domain.user.infraestructure.persistence.entity.RefreshToken;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenMapperImpl implements RefreshTokenMapper {
    @Override
    public RefreshTokenDomain toDomain(RefreshToken refreshToken) {
        if (refreshToken == null) return null;
        return new RefreshTokenDomain(
                refreshToken.getId(),
                refreshToken.getRefreshToken(),
                refreshToken.getUserAvalonId(),
                refreshToken.getExpiryDate(),
                refreshToken.isRevoked(),
                refreshToken.getIssuedAt()
        );
    }

    @Override
    public RefreshToken toEntity(RefreshTokenDomain domain) {
        if (domain == null) return null;
        return new RefreshToken(
                domain.getId(),
                domain.getRefreshToken(),
                domain.getUserAvalonId(),
                domain.getExpiryDate(),
                domain.isRevoked(),
                domain.getIssuedAt()
        );
    }
}
