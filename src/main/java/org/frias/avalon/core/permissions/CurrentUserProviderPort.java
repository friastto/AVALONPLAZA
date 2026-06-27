package org.frias.avalon.core.permissions;

/**
 * Puerto de abstracción para obtener los datos del usuario autenticado actual y el tenant.
 * Permite desacoplar los Casos de Uso (capa de aplicación/dominio) de la infraestructura de
 * seguridad (Spring Security) y el almacenamiento local del hilo (TenantContext ThreadLocal).
 */
public interface CurrentUserProviderPort {

    /**
     * Obtiene el contexto de seguridad del usuario autenticado en la petición actual.
     *
     * @return {@link UserContext} con los datos del usuario.
     */
    UserContext getCurrentUserContext();

    /**
     * Obtiene el ID de la tienda (Outlet) asociada al empleado autenticado en la petición actual.
     *
     * @return El ID de la tienda, o {@code null} si es un usuario global o no autenticado.
     */
    Long getCurrentOutletId();

    /**
     * Obtiene el ID del tenant (Empresa) asociado a la petición actual.
     *
     * @return El ID del tenant, o {@code null} si es una petición global.
     */
    Long getCurrentTenantId();

    /**
     * Verifica si el usuario actual posee un rol específico de seguridad.
     *
     * @param role El rol a comprobar (ej. "ROLE_ADMIN").
     * @return {@code true} si posee el rol, {@code false} en caso contrario.
     */
    boolean hasRole(String role);
}
