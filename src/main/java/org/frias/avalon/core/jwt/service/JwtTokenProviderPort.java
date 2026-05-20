package org.frias.avalon.core.jwt.service;

import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;

public interface JwtTokenProviderPort {

    String generateAccessToken(UserDetails userDetails, Long outletId);

    String generateAccessTokenFromId(Long userId);

    String generateRefreshTokenFromId(Long userId);
    
    boolean validateToken(String token);

    String extractUsername(String token);

    List<String> extractRoles(String token);

    Long extractOutletId(String token);
    
    Long extractClaimAsLong(String token, String claimName);
}
