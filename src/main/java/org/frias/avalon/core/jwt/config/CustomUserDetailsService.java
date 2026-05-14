package org.frias.avalon.core.jwt.config;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService { // Implementa UserDetailsService

    private final UserAvalonRepositoryPort userPort;
    private final MasterTreeProvider treeProvider;
    private final RoleAssignmentRepositoryPort rolePort;

    public CustomUserDetailsService(UserAvalonRepositoryPort userPort, MasterTreeProvider treeProvider, RoleAssignmentRepositoryPort rolePort) {
        this.userPort = userPort;
        this.treeProvider = treeProvider;

        this.rolePort = rolePort;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Buscar el usuario en la base de datos
        UserAvalonDomain usuario = userPort.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // 2. Obtener el MasterTree para acceder a los datos maestros
        MasterTree masterTree = treeProvider.getTree();

        // 3. Determinar el estado de la cuenta del usuario
        MasterRoot userStatus = masterTree.getById(usuario.getStatusId());


        if (userStatus == null) {
            throw new IllegalStateException("Estado de usuario inconsistente en caché para: " + username);
        }

        boolean enabled = masterTree.is(userStatus, "ACT"); // Asumimos "ACT" es el shortName para activo
        boolean accountNonLocked = !masterTree.is(userStatus, "LOKUSER") && !masterTree.is(userStatus, "BAN"); // No bloqueado si no es LOKUSER ni BAN
        boolean credentialsNonExpired = true; // Asumimos que las credenciales no expiran por defecto
        boolean accountNonExpired = true;    // Asumimos que la cuenta no expira por defecto

        // 4. Cargar y procesar los roles asignados al usuario
        List<RoleAssignmentDomain> roleAssignments = rolePort.findByUser(usuario.getId());

        Collection<SimpleGrantedAuthority> authorities = roleAssignments.stream()
                .filter(assignment -> {
                    // Filtrar solo asignaciones de rol activas
                    MasterRoot assignmentStatus = masterTree.getById(assignment.getStatus());
                    return assignmentStatus != null && masterTree.is(assignmentStatus, "ACT"); // Asumimos "ACT" para asignaciones activas
                })
                .map(assignment -> {
                    // Convertir el ID del rol a su MasterRoot y luego a SimpleGrantedAuthority
                    MasterRoot roleMasterRoot = masterTree.getById(assignment.getRoleId());
                    if (roleMasterRoot == null) {
                        // Loggear o manejar roles inconsistentes, por ahora lo ignoramos
                        return null;
                    }
                    // Convención: los roles en Spring Security suelen ir prefijados con "ROLE_"
                    return new SimpleGrantedAuthority("ROLE_" + roleMasterRoot.getShortName().toUpperCase());
                })
                .filter(java.util.Objects::nonNull) // Eliminar cualquier autoridad nula si un rol no se encontró
                .collect(Collectors.toSet()); // Usar un Set para evitar roles duplicados

        // Si el usuario no tiene roles asignados o activos, se le puede asignar un rol por defecto si es necesario
        if (authorities.isEmpty()) {
            // Ejemplo: asignar un rol de "USUARIO" por defecto si no tiene ninguno
            authorities.add(new SimpleGrantedAuthority("ROLE_USANONIMO"));
        }

        // 5. Devolver un objeto UserDetails que Spring Security entiende
        return new User(
                usuario.getUserName(),
                usuario.getHashPassword(),
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                authorities
        );
    }
}