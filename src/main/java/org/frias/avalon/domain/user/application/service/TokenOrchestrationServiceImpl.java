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
import java.util.Objects;
import java.util.UUID;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;

@Service
@AllArgsConstructor
public class TokenOrchestrationServiceImpl implements TokenOrchestrationService {
    private final JwtTokenProviderPort jwtTokenProvider;
    private final RefreshTokenRepositoryPort refreshTokenPort;
    private final UserAvalonOutletResolverService outletResolverService;
    private final MasterTreeProvider masterTreeProvider;

    @Override
    public TokenRefreshResult generateTokens(UserAvalonDomain user, UserDetails userDetails, List<RoleAssignmentDomain> roleAssigned) {

        OutletDomain outlet = outletResolverService.resolveActiveOutlet(roleAssigned);
        MasterTree tree = masterTreeProvider.getTree();

        // 1. Generar Access Token con Zero Trust claims (outlet_id y company_id)
        Long outletId = outlet != null ? outlet.getId() : null;
        Long companyId = outlet != null ? outlet.getCompanyId() : null;

        if (companyId == null && roleAssigned != null) {
            companyId = roleAssigned.stream()
                    .filter(r -> r.getCompanyId() != null && tree.is(tree.getById(r.getStatus()), "ACT"))
                    .map(RoleAssignmentDomain::getCompanyId)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        // Validacion estricta Zero Trust para GERGEN
        if (roleAssigned != null) {
            boolean hasGergenRole = roleAssigned.stream()
                    .anyMatch(r -> tree.is(tree.getById(r.getRoleId()), "GERGEN") && tree.is(tree.getById(r.getStatus()), "ACT"));
            if (hasGergenRole && companyId == null) {
                throw new BusinessException("El rol GERGEN requiere una empresa valida asignada");
            }
        }

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails, outletId, companyId, user.getId());

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
