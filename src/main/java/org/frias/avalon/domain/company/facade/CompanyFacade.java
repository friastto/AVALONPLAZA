package org.frias.avalon.domain.company.facade;

import org.frias.avalon.domain.user.domain.services.interfaces.UsersService;

public class CompanyFacade extends TenantSecurity {

    private final UsersService usersService;

    public CompanyFacade(UsersService usersService) {
        super();
        this.usersService = usersService;
    }
/*
    public List<UserAvalon> listarUsuariosSegunContexto() {
        String rolActual = getRol();

        // Si es Staff de Avalon, ve a TODOS los de todas las empresas
        if (isMasterStaff()) {
            return usersService.();
        }

        // Si es un Gerente de Company, ve a todos los de SU empresa (incluye todas sus outlets)
        if (ROLES_COMPANY.contains(rolActual)) {
            return usersService.sea(getValidatedCompanyId());
        }

        // Si es un Supervisor de Outlet, solo ve a sus compañeros de tienda
        if (ROLES_OUTLET.contains(rolActual)) {
            return usersService.findAllByOutletId(getValidatedOutletId());
        }

        // Si es un Cliente, solo puede verse a sí mismo (o nada)
        throw new SecurityException("No tienes permisos para listar usuarios.");
    }

 */
}
