package org.frias.avalon.core.jwt.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.attribute.UserPrincipal;

public class SecurityUtils {

    // Para saber si el usuario tiene un rol específico (ej. ROLE_ADMIN)
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) return false;

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(role));
    }

    // Para obtener el name de usuario o email del token
    public static String getCurrentUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return null;
    }
    public static Long getTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) return null;

        Object principal = authentication.getPrincipal();


        return null;
    }
}