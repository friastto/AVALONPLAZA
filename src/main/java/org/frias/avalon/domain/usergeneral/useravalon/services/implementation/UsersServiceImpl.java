package org.frias.avalon.domain.usergeneral.useravalon.services.implementation;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.company.facade.BaseTenantService;
import org.frias.avalon.domain.company.services.interfaces.CompanyService;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataService;
import org.frias.avalon.domain.usergeneral.useravalon.dtos.request.UserNewLinkPersonDto;
import org.frias.avalon.domain.usergeneral.useravalon.dtos.request.UserNewDto;
import org.frias.avalon.domain.usergeneral.useravalon.entities.UserAvalon;
import org.frias.avalon.domain.usergeneral.useravalon.repositories.UserRepository;
import org.frias.avalon.domain.usergeneral.useravalon.services.interfaces.EmployeeService;
import org.frias.avalon.domain.usergeneral.useravalon.services.interfaces.UsersService;
import org.frias.avalon.domain.usergeneral.useravalon.services.interfaces.UsuarioServiceValidate;
import org.frias.avalon.domain.usergeneral.auth.dtos.request.UserValidateCredentials;
import org.frias.avalon.core.jwt.util.JwtUtils;
import org.frias.avalon.domain.person.entity.Person;
import org.frias.avalon.domain.person.services.interfaces.PersonService;
import org.frias.avalon.core.util.PassSecure;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsersServiceImpl extends BaseTenantService implements UsersService, UsuarioServiceValidate, EmployeeService {


    private final UserRepository userRepository;
    private final PersonService personService;
    private final MasterDataService masterDataService;
    private final JwtUtils jwtUtils;
    private final CompanyService companyService;

    public UsersServiceImpl(UserRepository userRepository, PersonService personService, MasterDataService masterDataService, JwtUtils jwtUtils, CompanyService companyService) {
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

        userRepository.findByUserName(userCreate.userName())
                .ifPresent(u -> {
                    throw new EntityExistsException("el name de usuario no esta disponible");
                });

        Person personEntity = personService.save(userCreate.newPersonData());

        return createUserAndCreateLinkPerson(new UserNewLinkPersonDto(
                userCreate.userName(),
                userCreate.password(),
                userCreate.role(),
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
    public Boolean validateUser(UserValidateCredentials userValidateCredentials) {

        UserAvalon user = userRepository.findByUserName(userValidateCredentials.userName())
                .orElseThrow(() -> {
                    throw new RuntimeException("credenciales invalidas");
                });

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





}



