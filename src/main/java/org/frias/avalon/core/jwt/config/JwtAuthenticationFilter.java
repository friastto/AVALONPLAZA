package org.frias.avalon.core.jwt.config;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.frias.avalon.core.jwt.service.JwtTokenProviderPort;
import org.frias.avalon.core.jwt.service.SessionRevocationRegistry;
import org.frias.avalon.core.jwt.util.SecurityUtils;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtTokenProviderPort jwtTokenProvider;
    private final MasterTreeProvider treeProvider;
    private final CustomUserDetailsService userDetailsService;
    private final OutletRepositoryPort outletRepositoryPort;
    private final SessionRevocationRegistry sessionRevocationRegistry;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;

    public JwtAuthenticationFilter(
            JwtTokenProviderPort jwtTokenProvider,
            MasterTreeProvider treeProvider,
            CustomUserDetailsService userDetailsService,
            OutletRepositoryPort outletRepositoryPort,
            SessionRevocationRegistry sessionRevocationRegistry,
            UserAvalonRepositoryPort userAvalonRepositoryPort
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.treeProvider = treeProvider;
        this.userDetailsService = userDetailsService;
        this.outletRepositoryPort = outletRepositoryPort;
        this.sessionRevocationRegistry = sessionRevocationRegistry;
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Si la petición va a auth (y no es suplantar) o a la creación de usuario, el filtro NO se ejecuta
        return path.startsWith("/avalon/auth") || path.startsWith("/avalon/user/create");
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

                    // 2.1 Verificamos si la sesion del usuario fue revocada (inactivacion/traslado)
                    String username = jwtTokenProvider.extractUsername(jwt);
                    Long userIdFromJwt = jwtTokenProvider.extractUserId(jwt);
                    if (userIdFromJwt == null && username != null) {
                        userIdFromJwt = userAvalonRepositoryPort.findByUserName(username)
                                .map(UserAvalonDomain::getId)
                                .orElse(null);
                    }
                    Instant issuedAt = jwtTokenProvider.extractIssuedAt(jwt);
                    if (userIdFromJwt != null && sessionRevocationRegistry.isRevoked(userIdFromJwt, issuedAt)) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"status\": 401, \"message\": \"Sesion revocada por cambio de estado o permisos\", \"data\": null}");
                        return;
                    }

                    // 3. Extraemos rol desde el token
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




                    Long outletIdFromJwt = jwtTokenProvider.extractOutletId(jwt);
                    Long companyId = jwtTokenProvider.extractCompanyId(jwt);
                    Long outletId = outletIdFromJwt;

                    String companyHeader = request.getHeader("X-Company-Id");
                    String outletHeader = request.getHeader("X-Outlet-Id");

                    boolean hasAdminRole = SecurityUtils.hasRole("ROLE_ADMINTI") || SecurityUtils.hasRole("ROLE_ADMINSYS") || SecurityUtils.hasRole("ROLE_ADMIN");
                    boolean hasGergenRole = SecurityUtils.hasRole("ROLE_GERGEN");

                    if (companyHeader != null && !companyHeader.isBlank()) {
                        try {
                            Long requestedCompanyId = Long.parseLong(companyHeader.trim());
                            if (hasAdminRole || (companyId != null && companyId.equals(requestedCompanyId))) {
                                companyId = requestedCompanyId;
                            } else {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"status\": 403, \"message\": \"No autorizado para cambiar de empresa\", \"data\": null}");
                                return;
                            }
                        } catch (NumberFormatException e) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"status\": 400, \"message\": \"X-Company-Id invalido\", \"data\": null}");
                            return;
                        }
                    }

                    if (outletHeader != null && !outletHeader.isBlank()) {
                        try {
                            Long requestedOutletId = Long.parseLong(outletHeader.trim());
                            if (hasAdminRole || (outletIdFromJwt != null && outletIdFromJwt.equals(requestedOutletId))) {
                                outletId = requestedOutletId;
                            } else if (hasGergenRole) {
                                final Long currentCompanyId = companyId;
                                boolean belongsToCompany = outletRepositoryPort.findById(requestedOutletId)
                                        .map(o -> o.getCompanyId() != null && o.getCompanyId().equals(currentCompanyId))
                                        .orElse(false);
                                if (belongsToCompany) {
                                    outletId = requestedOutletId;
                                } else {
                                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                    response.setContentType("application/json;charset=UTF-8");
                                    response.getWriter().write("{\"status\": 403, \"message\": \"No autorizado: La tienda no pertenece a su empresa\", \"data\": null}");
                                    return;
                                }
                            } else {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"status\": 403, \"message\": \"No autorizado para cambiar de tienda\", \"data\": null}");
                                return;
                            }
                        } catch (NumberFormatException e) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"status\": 400, \"message\": \"X-Outlet-Id invalido\", \"data\": null}");
                            return;
                        }
                    }

                    // 8. Continuamos con el resto del pipeline
                    // Ponemos el company y el outlet en el contexto
                    if (companyId != null) {
                        TenantContext.setTenantId(companyId);
                    }
                    if (outletId != null) {
                        TenantContext.setTenantOutletId(outletId);
                    }
                    // --- Logica para clasificar y establecer roles especificos en TenantContext ---
                    MasterTree masterTree = treeProvider.getTree();
                    String employeeRoleCode = null;
                    String consumerRoleCode = null;
                    Long employeeOutletIdForContext = null; // El outletId que realmente se pondra en TenantContext

                    for (String roleCode : rolesFromJwt) {
                        MasterRoot roleMasterRoot = masterTree.getByCode(roleCode);
                        if (roleMasterRoot != null) {
                            // Clasificar como Empleado
                            // Priorizamos roles de EMPLEADO DE OUTLET (descendientes de EMP, pero no de SISTEM)
                            if (masterTree.isChildOf(roleMasterRoot, "EMP") && !masterTree.isChildOf(roleMasterRoot, "SISTEM")) {
                                if (employeeRoleCode == null) { // Tomamos el primer rol de empleado de outlet que encontremos
                                    employeeRoleCode = roleCode;
                                    // Si este es un rol de empleado de OUTLET y el JWT tiene un outletId, lo asociamos
                                    if (!"GERGEN".equals(roleCode) && outletIdFromJwt == null && outletId == null) {
                                        throw new SecurityException("Employee role detected but no outlet assigned in token");
                                    }
                                    employeeOutletIdForContext = outletId != null ? outletId : outletIdFromJwt;
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