package org.frias.avalon.core.jwt.util;

import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.core.tenant.TenantContext; // Asegurarse de que esta importación esté presente
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

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

    // Eliminado el método getTenantId() de SecurityUtils, ya que se obtiene directamente de TenantContext.

    /**
     * Construye el UserContext a partir de la autenticación actual del usuario.
     * Asume que la autenticación proviene de una configuración estándar de Spring Security.
     * Los datos de companyId y employeeOutletId se obtienen del TenantContext.
     *
     * @return UserContext con la información del usuario actual.
     * @throws IllegalStateException si no hay un usuario autenticado.
     */
    public static UserContext getCurrentUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No user authenticated.");
        }

        // Extraer roles
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_")) // Asegurarse de que sean roles
                .collect(Collectors.toList());

        // Extraer username
        String username = authentication.getName();

        // Obtener companyId y employeeOutletId del TenantContext
        //Long companyId = TenantContext.getTenantId();
        Long employeeOutletId = TenantContext.getTenantOutletId();

        return new UserContext(
                username,
                roles,
                employeeOutletId
                // Ahora se pasa companyId
        );
    }
}
