package org.frias.avalon.domain.user.application.service;

import lombok.AllArgsConstructor;
import org.frias.avalon.core.jwt.service.JwtTokenProviderPort;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.user.application.dtos.response.TokenRefreshResult;
import org.frias.avalon.domain.user.domain.model.RefreshTokenDomain;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RefreshTokenRepositoryPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TokenOrchestrationServiceImpl implements TokenOrchestrationService {
    private final JwtTokenProviderPort jwtTokenProvider;
    private final RefreshTokenRepositoryPort refreshTokenPort;
    private final UserAvalonOutletResolverService outletResolverService;

    @Override
    public TokenRefreshResult generateTokens(UserAvalonDomain user, UserDetails userDetails, List<RoleAssignmentDomain> roleAssigned) {

        OutletDomain outlet = outletResolverService.resolveActiveOutlet(roleAssigned);

        // 1. Generar Access Token
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails, outlet != null ? outlet.getId() : null);

        // 2. Generar y persistir Refresh Token
        UUID refreshTokenUuid = jwtTokenProvider.generateRefreshToken();
        Instant refreshTokenExpiryDate = jwtTokenProvider.timeRefreshTokenExpiration();

        RefreshTokenDomain refreshtokenData = new RefreshTokenDomain(
                refreshTokenUuid.toString(),
                user.getId(),
                refreshTokenExpiryDate
        );

        refreshTokenPort.save(refreshtokenData);

        return new TokenRefreshResult(accessToken, refreshTokenUuid.toString());
    }
}
