package org.frias.avalon.core.jwt.config;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.frias.avalon.core.jwt.service.JwtTokenProviderPort;
import org.frias.avalon.core.jwt.util.SecurityUtils;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtTokenProviderPort jwtTokenProvider;
    private final MasterTreeProvider treeProvider;


    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProviderPort jwtTokenProvider, MasterTreeProvider treeProvider, CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.treeProvider = treeProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Si la petición va a auth (y no es suplantar) o a la creación de usuario, el filtro NO se ejecuta
        return (path.startsWith("/avalon/auth") && !path.startsWith("/avalon/auth/impersonate")) 
                || path.startsWith("/avalon/user/create");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, java.io.IOException {

        try { // Es buena práctica envolver esto para limpiar el contexto al final
            final String authHeader = request.getHeader("Authorization");

            // 1. Validamos que el header comience con "Bearer "
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7); // quitamos "Bearer "

                // 2. Validamos el token antes de hacer nada más
                if (jwtTokenProvider.validateToken(jwt)) {

                    // 3. Extraemos userName y rol desde el token
                    String username = jwtTokenProvider.extractUsername(jwt);
                    List<String> rolesFromJwt = jwtTokenProvider.extractRoles(jwt);

                    // 4. Si no hay una autenticación activa en el contexto
                    Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

                    if (username != null && (currentAuth == null || currentAuth instanceof AnonymousAuthenticationToken)) {

                        // 5. Creamos autoridades desde los roles del token de forma individual (con prefijo ROLE_)
                        List<GrantedAuthority> authorities = rolesFromJwt.stream()
                                .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase()))
                                .collect(Collectors.toList());

                        // 6. Creamos autenticación con userName y autoridad (sin password ni detalles)
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(username, jwt, authorities);

                        // 7. Registramos la autenticación en el contexto de seguridad
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }


                    if (rolesFromJwt != null && !rolesFromJwt.isEmpty())
                        System.out.println("ROLES DEL TOKEN " + rolesFromJwt.toString());

                    SecurityContextHolder.getContext().getAuthentication()
                            .getAuthorities()
                            .forEach(a -> System.out.println("AUTH: " + a.getAuthority()));

                    Long outletIdFromJwt = jwtTokenProvider.extractOutletId(jwt);

                    String tenantHeader = request.getHeader("X-Tenant-Id");

                    // 🔐 solo ADMINTI puede usar header
                    if (!SecurityUtils.hasRole("ROLE_ADMINTI") && tenantHeader != null) {
                        throw new SecurityException("No autorizado para cambiar tenant");
                    }

                    // si viene header, lo se parsea seguro
                    Long companyId = jwtTokenProvider.extractClaimAsLong(jwt, "empresa_Id");

                    if (tenantHeader != null) {
                        try {
                            companyId = Long.parseLong(tenantHeader);
                        } catch (NumberFormatException e) {
                            throw new SecurityException("X-Tenant-Id inválido");
                        }
                    }


                    // 8. Continuamos con el resto del pipeline
                    //ponemos el company y el outlet en el contexto
                    if (companyId != null) {
                        TenantContext.setTenantId(companyId);
                    }
                    if (outletIdFromJwt != null) {
                        TenantContext.setTenantOutletId(outletIdFromJwt);

                    }
                    // --- Lógica para clasificar y establecer roles específicos en TenantContext ---
                    MasterTree masterTree = treeProvider.getTree();
                    String employeeRoleCode = null;
                    String consumerRoleCode = null;
                    Long employeeOutletIdForContext = null; // El outletId que realmente se pondrá en TenantContext

                    for (String roleCode : rolesFromJwt) {
                        MasterRoot roleMasterRoot = masterTree.getByCode(roleCode);
                        if (roleMasterRoot != null) {
                            // Clasificar como Empleado
                            // Priorizamos roles de EMPLEADO DE OUTLET (descendientes de EMP, pero no de SISTEM)
                            if (masterTree.isChildOf(roleMasterRoot, "EMP") && !masterTree.isChildOf(roleMasterRoot, "SISTEM")) {
                                if (employeeRoleCode == null) { // Tomamos el primer rol de empleado de outlet que encontremos
                                    employeeRoleCode = roleCode;
                                    // Si este es un rol de empleado de OUTLET y el JWT tiene un outletId, lo asociamos
                                    if (outletIdFromJwt == null) {
                                        throw new SecurityException("se detecto qeu el suuario tiene un rol de empleado pero no tiene una outlet asignada no pude continuar con la autenticacion");
                                    }
                                    employeeOutletIdForContext = outletIdFromJwt;
                                }
                            }
                            // Luego, intentamos clasificar como EMPLEADO GLOBAL (descendientes de SISTEM)
                            // Esto se ejecuta solo si no se encontró un rol de empleado de outlet antes
                            else if (masterTree.isChildOf(roleMasterRoot, "SISTEM")) {
                                if (employeeRoleCode == null) { // Tomamos el primer rol de empleado global que encontremos
                                    employeeRoleCode = roleCode;
                                    // Para empleados globales, employeeOutletIdForContext permanece null
                                }
                            }

                            // Clasificar como Consumidor
                            if (masterTree.isChildOf(roleMasterRoot, "CONS")) {
                                if (consumerRoleCode == null) { // Tomamos el primer rol de consumidor que encontremos
                                    consumerRoleCode = roleCode;
                                }
                            }
                        }
                    }

                    // Establecer los roles y el outletId en el TenantContext
                    if (employeeRoleCode != null) {
                        TenantContext.setTenantRolEmployee(employeeRoleCode);
                        if (employeeOutletIdForContext != null) {
                            TenantContext.setTenantOutletId(employeeOutletIdForContext);
                        }
                    }
                    if (consumerRoleCode != null) {
                        TenantContext.setTenantRolConsumer(consumerRoleCode);
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