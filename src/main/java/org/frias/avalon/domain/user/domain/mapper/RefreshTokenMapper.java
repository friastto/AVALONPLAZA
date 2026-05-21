package org.frias.avalon.domain.user.domain.mapper;

import org.frias.avalon.domain.user.domain.model.RefreshTokenDomain;
import org.frias.avalon.domain.user.infraestructure.persistence.entity.RefreshToken;

public interface RefreshTokenMapper {

    RefreshTokenDomain toDomain(RefreshToken refreshToken);

    RefreshToken toEntity(RefreshTokenDomain domain);
}
