package org.frias.avalon.infraestructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.frias.avalon.core.jwt.service.JwtTokenProviderPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtTokenProviderAdapter implements JwtTokenProviderPort {

    private final SecretKey jwtSecretKey;
    private final long jwtExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProviderAdapter(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration-ms}") long jwtExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshTokenExpirationMs) {
        
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpirationMs = jwtExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Override
    public String generateAccessToken(UserDetails userDetails, Long outletId) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(jwtExpirationMs);

        io.jsonwebtoken.JwtBuilder tknBuilder = Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("rol", extractCleanRole(userDetails))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate));

        if (outletId != null) {
            tknBuilder.claim("outlet_Id", outletId);
        }

        return tknBuilder.signWith(jwtSecretKey).compact();
    }

    @Override
    public String generateAccessTokenFromId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("El ID de usuario no puede ser nulo para generar un token de acceso.");
        }

        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(jwtExpirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(jwtSecretKey)
                .compact();
    }

    @Override
    public String generateRefreshTokenFromId(Long userId) {
        return UUID.randomUUID().toString();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Token inválido o expirado: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    @Override
    public List<String> extractRoles(String token) {
        return extractAllClaims(token).get("rol", List.class);
    }

    @Override
    public Long extractOutletId(String token) {
        return extractClaimAsLong(token, "outlet_Id");
    }

    @Override
    public Long extractClaimAsLong(String token, String claimName) {
        Claims claims = extractAllClaims(token);
        Object claimObj = claims.get(claimName);

        if (claimObj == null) {
            return null;
        }
        if (claimObj instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(claimObj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private List<String> extractCleanRole(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toList());
    }
}
