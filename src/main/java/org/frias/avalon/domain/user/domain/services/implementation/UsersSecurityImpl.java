package org.frias.avalon.domain.user.domain.services.implementation;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.jwt.util.JwtUtils;
import org.frias.avalon.core.util.PassSecure;
import org.frias.avalon.domain.company.domain.entities.Company;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.company.application.services.interfaces.CompanyService;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataService;
import org.frias.avalon.domain.person.entity.Person;
import org.frias.avalon.domain.person.services.interfaces.PersonService;
import org.frias.avalon.domain.user.domain.dtos.request.UserValidateCredentials;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewLinkPersonDto;
import org.frias.avalon.domain.user.domain.entities.UserAvalon;
import org.frias.avalon.domain.user.infraestruture.repositories.UserRepository;
import org.frias.avalon.domain.user.domain.services.interfaces.EmployeeService;
import org.frias.avalon.domain.user.domain.services.interfaces.UsersService;
import org.frias.avalon.domain.user.domain.services.interfaces.UsuarioServiceValidate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UsersSecurityImpl extends TenantSecurity implements UsersService, UsuarioServiceValidate, EmployeeService {


    private final UserRepository userRepository;
    private final PersonService personService;
    private final MasterDataService masterDataService;
    private final JwtUtils jwtUtils;
    private final CompanyService companyService;

    private static final Map<String, Set<String>> ROLE_PERMISSIONS = Map.of(
            "ADMINTI", Set.of("ROOT", "ADMIN", "GERGEN", "USER","GERENTE"),
            "ADMIN", Set.of("GERGEN", "USER"),
            "GERENTE", Set.of("USER")
    );

    public UsersSecurityImpl(UserRepository userRepository, PersonService personService, MasterDataService masterDataService, JwtUtils jwtUtils, CompanyService companyService) {
        this.userRepository = userRepository;
        this.personService = personService;
        this.masterDataService = masterDataService;
        this.jwtUtils = jwtUtils;
        this.companyService = companyService;

    }


    @Override
    public UserAvalon searchByUserName(String userName) {


        return userRepository.findByUserName(userName)
                .orElseThrow();

    }

    @Override
    public UserAvalon searchById(Long id) {

       return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("usuario no encontrado"));
    }

    @Transactional
    @Override
    public UserAvalon createUserAndPerson(UserNewDto userCreate) {

        // se construye la entidad Usuario


        Person personEntity = personService.save(userCreate.newPersonData());

        return createUserAndCreateLinkPerson(new UserNewLinkPersonDto(
                userCreate.userName(),
                userCreate.password(),
                userCreate.roleId(),
                personEntity.getId(),
                userCreate.companyId(),
                userCreate.outletId()
        ));

    }

    @Override
    public UserAvalon create(UserNewLinkPersonDto request) {





        return null;
    }

    @Transactional
    @Override
    public UserAvalon createUserAndCreateLinkPerson(UserNewLinkPersonDto userCreate) {

        // 1. OBTENCIÓN DE CONTEXTOS (Usando BaseTenantService)
        Long idCompanyContext = getCompanyId();
        String operatorRol = getRol();

        // 2. DATOS DEL NUEVO USUARIO Y SU JERARQUÍA
        MasterData rolNewUser = masterDataService.searchById(userCreate.roleId());
        MasterData rolRootNew = masterDataService.getRootBranch(rolNewUser.getId(), "ROL");
        String rootNewName = rolRootNew.getShortName();

        UserAvalon uEntity = new UserAvalon();

        // 3. MOTOR DE REGLAS SIMPLIFICADO
        if (operatorRol == null) {
            // --- REGISTRO PÚBLICO / ANÓNIMO ---
            rolNewUser = masterDataService.searchByShortName("INVITADO");
            uEntity.setCompanyId(null);
        }
        else if (isMasterStaff()) {
            // --- CASO STAFF AVALON (ROOT) ---
            // Tú decides si el usuario es global (null) o de una empresa específica
            uEntity.setCompanyId(userCreate.companyId() != null ?
                    companyService.searchById(userCreate.companyId()) : null);
        }
        else if (idCompanyContext != null) {
            // --- CASO EMPRESA CLIENTE (Gerentes/Directores) ---

            // SEGURIDAD: Un cliente no puede crear roles del Staff de Avalon
            if (ROLES_AVALON.contains(rootNewName)) {
                throw new SecurityException("No puedes asignar roles de nivel Master (Avalon Staff).");
            }

            // JERARQUÍA: Un Gerente no puede crear un Director (Jerarquía Superior)
            if ("GERENTE".equals(operatorRol) && "DIREC".equals(rootNewName)) {
                throw new SecurityException("Jerarquía insuficiente: Un Gerente no puede crear Directivos.");
            }

            // SELLO DE TENANT: Hereda obligatoriamente la empresa del creador
            uEntity.setCompanyId(companyService.searchById(idCompanyContext));
        }
        else {
            throw new SecurityException("Tu rol actual (" + operatorRol + ") no tiene permisos de creación.");
        }
        // --- NUEVO: 4. VALIDACIÓN DE IDENTIDAD DUAL (REGLA DE ROBERTO) ---
        // Buscamos todos los usuarios asociados a esa persona
        List<UserAvalon> existingUsers = userRepository.findAllByPersonId(userCreate.personId());

        for (UserAvalon u : existingUsers) {
            Long userCompanyId = (u.getCompanyId() != null) ? u.getCompanyId().getId() : null;

            // Caso A: Ya tiene usuario de empleado en esta misma empresa
            if (idCompanyContext != null && idCompanyContext.equals(userCompanyId)) {
                throw new EntityExistsException("Esta persona ya tiene un usuario de empleado en esta empresa (" + u.getUserName() + ").");
            }

            // Caso B: Ya es empleado de OTRA empresa (bloqueo por seguridad multi-tenant)
            if (userCompanyId != null && !userCompanyId.equals(idCompanyContext)) {
                throw new SecurityException("Esta persona ya está vinculada como empleado en otra organización.");
            }

            // NOTA: Si userCompanyId es NULL, es un INVITADO.
            // El bucle lo ignora y permite que el flujo continúe para crear el nuevo usuario de empleado.
        }

        // 4. VALIDACIONES DE INTEGRIDAD
        userRepository.findByUserName(userCreate.userName()).ifPresent(u -> {
            throw new EntityExistsException("El name de usuario '" + userCreate.userName() + "' ya existe.");
        });

        // 5. CONSTRUCCIÓN Y PERSISTENCIA
        Person personEntity = personService.searchById(userCreate.personId());

        uEntity.setPerson(personEntity);
        uEntity.setRolId(rolNewUser);
        uEntity.setUserName(userCreate.userName());
        uEntity.setHashSalt(PassSecure.generateSalt());
        uEntity.setHashPassword(PassSecure.hashPassword(userCreate.password(), uEntity.getHashSalt()));
        uEntity.setStatusId(masterDataService.searchByShortName("ACT"));

        return userRepository.save(uEntity);
    }




    @Transactional
    @Override
    public UserAvalon clear(Long id) {

        UserAvalon ua = searchById(id);

        MasterData statusEliminado = masterDataService.searchByShortName("DEL");

        ua.setStatusId(statusEliminado);

        return userRepository.save(ua);
    }

    @Transactional
    @Override
    public UserAvalon changeStatus(Long idUser,Long idStatus ) {

        UserAvalon ua = searchById(idUser);

        MasterData statusEliminado = masterDataService.searchById(idStatus);

        ua.setStatusId(statusEliminado);

        return userRepository.save(ua);
    }

    @Override
    public UserAvalon create(Long idCompany, String name, String password, Long role) {

        Company company= companyService.searchById(idCompany);

        UserAvalon userAdminCompany = new UserAvalon();

        userAdminCompany.setCompanyId(company);

        userAdminCompany.setUserName(name);
        userAdminCompany.setRolId(masterDataService.searchById(role));

        final String hashSalt = PassSecure.generateSalt();

        userAdminCompany.setHashSalt(hashSalt);
        userAdminCompany.setHashPassword(PassSecure.hashPassword(password, hashSalt));


        userAdminCompany.setStatusId(masterDataService.getStatusActive());

        return userRepository.save(userAdminCompany);
    }

    @Override
    public UserAvalon createUserWithRules(Long companyId, String operatorRol, String username, String password, Long role) {

        return null;
    }

    @Override
    public UserAvalon createUser(Long idCompany, String rolOperator, Person person, String userName, String password, Long idRol) {
        // 🔹 username único
       validateUsername(userName);

        // 🔹 rol
        MasterData rolNewUser = masterDataService.searchById(idRol);
        MasterData rolRootNew = masterDataService.getRootBranch(rolNewUser.getId(), "ROL");

        //desactivar los uusarios en otras empresas
        deactivateActiveUsersInOtherCompanies(person.getId(), idCompany);

        // 🔹 validaciones
        validatePersonDoesNotHaveRoleInSameBranch(person.getId(), idRol);

        if (rolOperator != null) {
            validateHierarchy(rolOperator, rolRootNew);
        }

        validatePersonRules(idCompany, person.getId());

        // 🔹 empresa
        Company company = (idCompany != null)
                ? companyService.searchById(idCompany)
                : null;

        // 🔹 construir
        UserAvalon user = new UserAvalon();

        user.setCompanyId(company);
        user.setPerson(person);
        user.setRolId(rolNewUser);
        user.setUserName(userName);

        user.setHashSalt(PassSecure.generateSalt());
        user.setHashPassword(PassSecure.hashPassword(password, user.getHashSalt()));

        user.setStatusId(masterDataService.searchByShortName("ACT"));

        return userRepository.save(user);
    }

    @Override
    public boolean existsByPersonAndRole(Long personId, Long roleId) {

        return userRepository.existsByPersonAndRole(personId,roleId);
    }

    @Override
    public List<UserAvalon> getAllUserIntoCompany(Long idCompany) {

       return  userRepository.getAllEmployesCompany(idCompany);
    }


    @Override
    public Boolean validateUser(UserValidateCredentials userValidateCredentials) {

        UserAvalon user = userRepository.findByUserName(userValidateCredentials.userName())
                .orElseThrow(() ->
                     new IllegalArgumentException("credenciales invalidas")
                );

        return PassSecure.verifyPassword(userValidateCredentials.password(), user.getHashSalt(), user.getHashPassword());
    }


    /**
     * metodos del contexto de las empresas
     * @param idCompany
     * @return
     */

    @Override
    public List<UserAvalon> getAllEmployeesOnlyCompany(Long idCompany) {

        List<UserAvalon> userAvalonList = userRepository.getAllEmployeesOnlyCompany(idCompany);

        return userAvalonList.isEmpty() ? userAvalonList: List.of();
    }

    @Override
    public List<UserAvalon> getAllEmployeesOnlyOutlet(Long idOutlet) {

        List<UserAvalon> userAvalonList = userRepository.getAllEmployeesOnlyOutlet(idOutlet);

        return userAvalonList.isEmpty() ? userAvalonList: List.of();
    }

    @Override
    public List<UserAvalon> getAll(Long idCompany) {

        List<UserAvalon> userAvalonList = userRepository.getAllEmployesCompany(idCompany);

        return userAvalonList.isEmpty() ? userAvalonList: List.of();
    }

    private void validateHierarchy(String operatorRol, MasterData rolRootNew) {

        String newRole = rolRootNew.getShortName();

        Set<String> allowedRoles = ROLE_PERMISSIONS.get(operatorRol);

        if (allowedRoles == null || !allowedRoles.contains(newRole)) {
            throw new SecurityException(
                    "El rol " + operatorRol + " no puede crear usuarios con rol " + newRole
            );
        }
    }

    private void validatePersonRules(Long companyId, Long personId) {

        List<UserAvalon> existingUsers = userRepository.findAllByPersonId(personId);

        for (UserAvalon u : existingUsers) {

            Long userCompanyId = (u.getCompanyId() != null)
                    ? u.getCompanyId().getId()
                    : null;
            String status = u.getStatusId().getShortName();

            /*if ("ACT".equals(status)
                    && userCompanyId != null
                    && !userCompanyId.equals(companyId)) {

                throw new SecurityException(
                        "La persona ya está activa en otra empresa"
                );
            }*/
            if (companyId != null && companyId.equals(userCompanyId)) {
                throw new EntityExistsException("Ya tiene usuario en esta empresa");
            }


        }
    }
    private void validateUsername(String username) {

        userRepository.findByUserName(username)
                .ifPresent(u -> {
                    throw new EntityExistsException(
                            "El username '" + username + "' ya existe"
                    );
                });
    }
    private void validatePersonDoesNotHaveRoleInSameBranch(Long personId, Long roleId) {

        // 🔹 rol nuevo
        MasterData newRole = masterDataService.searchById(roleId);
        MasterData newRoot = masterDataService.getRootBranch(newRole.getId(), "ROL");

        // 🔹 usuarios actuales de la persona
        List<UserAvalon> users = userRepository.findAllByPersonId(personId);

        for (UserAvalon u : users) {

            MasterData existingRoot = masterDataService.getRootBranch(
                    u.getRolId().getId(),
                    "ROL"
            );

            if (existingRoot.getShortName().equals(newRoot.getShortName())) {
                throw new IllegalStateException(
                        "La persona ya tiene un usuario en la rama " + newRoot.getShortName()
                );
            }
        }
    }

    private void deactivateActiveUsersInOtherCompanies(Long personId, Long newCompanyId) {

        List<UserAvalon> users = userRepository.findAllByPersonId(personId);

        Map<Long, String> roleRootCache = new HashMap<>();

        for (UserAvalon u : users) {

            Long userCompanyId = (u.getCompanyId() != null)
                    ? u.getCompanyId().getId()
                    : null;

            String status = u.getStatusId().getShortName();

            String rootName = roleRootCache.computeIfAbsent(
                    u.getRolId().getId(),
                    roleId -> masterDataService
                            .getRootBranch(roleId, "ROL")
                            .getShortName()
            );

            if ("USUARIO".equals(rootName)) {
                continue;
            }

            if ("ACT".equals(status)
                    && userCompanyId != null
                    && !userCompanyId.equals(newCompanyId)) {

                u.setStatusId(masterDataService.searchByShortName("INA"));
            }
        }
    }
}



