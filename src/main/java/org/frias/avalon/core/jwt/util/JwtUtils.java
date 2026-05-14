package org.frias.avalon.core.jwt.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    @Value(value = "${app.jwt.secret}")
    private String jwtSecret;

    private final long jwtExpirationMs = 86400000;

    // 1. Generar el Token (Nuevo Builder)
    public String generateToken(UserDetails userDetails, Long outletId) {

        io.jsonwebtoken.JwtBuilder tknBuilder = Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("rol",extractCleanRole(userDetails))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs));


        /*if (empresaId != null) {
            tknBuilder.claim("empresa_Id", empresaId);
        }  */
        if (outletId != null) {

            tknBuilder.claim("outlet_Id", outletId);
        }


        return tknBuilder.signWith(getSigningKey()).compact();
    }

    private List<String> extractCleanRole(UserDetails userDetails) {
        // 1. Validamos que el usuario tenga al menos un rol
        // 1. Extraer rol con seguridad total
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .map(role -> role.replace("ROLE_", "")) // Quitar prefijo "ROLE_"
                .collect(Collectors.toList()); // Recolectar todos en una lista
    }

    // 2. Extraer todos los Claims (Método privado auxiliar para evitar repetir código)
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // Nuevo método en lugar de setSigningKey
                .build()
                .parseSignedClaims(token)    // Nuevo método en lugar de parseClaimsJws
                .getPayload();               // Nuevo método en lugar de getBody()
    }

    // 3. Extraer Username
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // 4. Extraer Rol
    // Modificado: Ahora extrae la lista de roles
    public List<String> extractRoles(String token) {
        // El claim "roles" se guarda como List<String>
        return extractAllClaims(token).get("roles", List.class);
    }


    //5. extrae la empresa asociada al usuario
    public Long extractCompany(String token) {
        Claims claims = extractAllClaims(token);

        // get() devolverá null si la llave "empresa_Id" no fue incluida en el builder
        Object empresaId = claims.get("empresa_Id");

        if (empresaId == null) {
            return null;
        }

        if (empresaId instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.parseLong(empresaId.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    public Long extractParent(String token,String parentName) {
        Claims claims = extractAllClaims(token);

        // get() devolverá null si la llave "empresa_Id" no fue incluida en el builder
        Object parentId = claims.get(parentName);

        if (parentId == null) {
            return null;
        }

        if (parentId instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.parseLong(parentId.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    // 6. Validar Token
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token); // Si el parseo falla o expira, lanzará excepción
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Token inválido o expirado: " + e.getMessage());
            return false;
        }
    }

    // 7. Obtener Llave de Firma (SecretKey)
    private SecretKey getSigningKey() {

        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
