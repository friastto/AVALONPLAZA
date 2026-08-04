package org.frias.avalon.core.jwt.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JwtTokenProviderPort {

    String generateAccessToken(UserDetails userDetails, Long outletId);

    String generateAccessToken(UserDetails userDetails, Long outletId, Long companyId);

    String generateImpersonationToken(UserDetails userDetails, Long outletId, List<String> roles);

    String generateAccessTokenFromId(Long userId);

    UUID generateRefreshToken();

    Instant timeRefreshTokenExpiration();
    
    boolean validateToken(String token);

    String extractUsername(String token);

    List<String> extractRoles(String token);

    Long extractOutletId(String token);

    Long extractCompanyId(String token);

    Long extractClaimAsLong(String token, String claimName);
}
