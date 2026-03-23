package org.frias.avalon.temp.jwt.config;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.frias.avalon.temp.empresasucursal.tenant.tenantcontex.TenantContext;
import org.frias.avalon.temp.jwt.util.JwtUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtUtils jwtUtils;


    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, CustomUserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, java.io.IOException {
        try { // Es buena práctica envolver esto para limpiar el contexto al final
            final String authHeader = request.getHeader("Authorization");

            // 1. Validamos que el header comience con "Bearer "
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7); // quitamos "Bearer "

               /* if (jwtUtils.validateToken(jwt)) {
                    System.out.println("✅ TOKEN VALIDADO CORRECTAMENTE PARA: " + jwtUtils.extractUsername(jwt));

                } else {
                    System.out.println("EL TOKEN NO ES VÁLIDO O ESTÁ EXPIRADO");
                }*/
                // 2. Validamos el token antes de hacer nada más
                if (jwtUtils.validateToken(jwt)) {

                    // 3. Extraemos username y rol desde el token
                    String username = jwtUtils.extractUsername(jwt);
                    String rol = jwtUtils.extractRol(jwt);

                    Long companyId = jwtUtils.extractParent(jwt,"empresa_Id");
                    Long outletId = jwtUtils.extractParent(jwt,"outlet_Id");

                    // 4. Si no hay una autenticación activa en el contexto
                    Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

                    if (username != null && (currentAuth == null || currentAuth instanceof AnonymousAuthenticationToken)) {

                        // 5. Creamos autoridad desde el rol del token
                        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol));

                        // 6. Creamos autenticación con username y autoridad (sin password ni detalles)
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(username, jwt, authorities);

                        // 7. Registramos la autenticación en el contexto de seguridad
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }

                    // 8. Continuamos con el resto del pipeline
                    //ponemos el company y el outlet en el contexto
                    if (companyId != null ) {
                        TenantContext.setTenantId(companyId);
                    }if (outletId != null ) {
                        TenantContext.setTenantOutletId(outletId);
                    }
                }

            }
                filterChain.doFilter(request, response);
        } finally {
            // 🚀 IMPORTANTE: Limpiar el ID al terminar la petición para que no se "filtre" a otro usuario
            TenantContext.clear();
        }


    }
}
/**
 * Se ejecuta en cada request para validar el JWT

@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException, java.io.IOException {

    final String authHeader = request.getHeader("Authorization");

    // Validamos que el header comience con "Bearer "
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String jwt = authHeader.substring(7); // quitamos "Bearer "
        String username = jwtUtils.extractUsername(jwt);
        String rol = jwtUtils.extractRol(jwt);

        // Si hay un username y no hay autenticación previa
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Validamos el token
            if (jwtUtils.validateToken(jwt)) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // Establecemos el usuario autenticado en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
    }

    filterChain.doFilter(request, response); // continúa con la cadena
}
}
*/
