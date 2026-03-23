package org.frias.avalon.domain.usergeneral.useravalon.services.implementation;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.temp.jwt.util.JwtUtils;
import org.frias.avalon.domain.company.A.CompanyService;
import org.frias.avalon.temp.empresasucursal.tenant.tenantcontex.TenantContext;
import org.frias.avalon.temp.features.auth.dtos.UserValidateCredentials;
import org.frias.avalon.domain.usergeneral.useravalon.dtos.UserLinkPersonRequestDto;
import org.frias.avalon.domain.usergeneral.useravalon.dtos.UserRequestNewDto;
import org.frias.avalon.domain.usergeneral.useravalon.entities.UserAvalon;
import org.frias.avalon.domain.usergeneral.useravalon.repositories.UserRepository;
import org.frias.avalon.domain.usergeneral.useravalon.services.interfaces.UsersService;
import org.frias.avalon.domain.usergeneral.useravalon.services.interfaces.UsuarioServiceValidate;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataService;
import org.frias.avalon.temp.person.entity.Person;
import org.frias.avalon.temp.person.services.interfaces.PersonService;
import org.frias.avalon.temp.util.PassSecure;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsersServiceImpl implements UsersService, UsuarioServiceValidate {


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
    public UserAvalon getUserEmployeeStatus(String numberId) {

        Optional<UserAvalon> user = userRepository.findActiveEmployeeByNumberId(numberId);


        if (user.isPresent()) {
            return user.get();
        }
        return null;
    }

    @Override
    public UserAvalon searchById(Long id) {

       return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("usuario no encontrado"));
    }

    @Transactional
    @Override
    public UserAvalon saveUserAndPerson(UserRequestNewDto userCreate) {

        // se construye la entidad Usuario

        userRepository.findByUserName(userCreate.userName())
                .ifPresent(u -> {
                    throw new EntityExistsException("el nombre de usuario no esta disponible");
                });

        Person personEntity = personService.save(userCreate.personId());

        return saveUserAndCreateLinkPerson(new UserLinkPersonRequestDto(
                userCreate.userName(),
                userCreate.password(),
                userCreate.role(),
                personEntity.getId()
        ));

    }

    @Transactional
    @Override
    public UserAvalon saveUserAndCreateLinkPerson(UserLinkPersonRequestDto userCreate) {

        UserAvalon uEntity = new UserAvalon();
        MasterData rolNewUser = null;

        // 1. Obtener el contexto de seguridad actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Identificar si es un registro público (sin login)
        boolean isAnonymous = auth == null ||
                !auth.isAuthenticated() ||
                auth instanceof AnonymousAuthenticationToken;

        if (!isAnonymous) {

            String token = (String) auth.getCredentials();

            String operatorRolName = jwtUtils.extractRol(token);

            Long idCompnayContext = TenantContext.getTenantId(); // Viene del Header X-Tenant-ID

            if (operatorRolName != null) {

                // Obtenemos el objeto del rol y su raíz jerárquica (ADMIN, DIREC, GERENTE, OPT, etc.)
                MasterData rolOperator = masterDataService.searchByShortName(operatorRolName);

                MasterData rolRootOperator = masterDataService.getRootBranch(rolOperator.getId(), "ROL");

                String rootName = rolRootOperator.getShortName();

                // Buscamos el rol que se quiere asignar al nuevo usuario
                rolNewUser = masterDataService.searchById(userCreate.roleId());

                MasterData rolRootNew = masterDataService.getRootBranch(rolNewUser.getId(), "ROL");

                String rootNewName = rolRootNew.getShortName();

                // --- MOTOR DE REGLAS DE JERARQUÍA (EL SWITCH INTELIGENTE) ---
                switch (rootName) {

                    case "ADMIN":
                        // El ADMIN de Avalon (SaaS) opera con TenantId en NULL
                        if (idCompnayContext == null) {
                            uEntity.setCompanyId(null);
                        } else {
                            // Un ADMIN de una Company no puede crear otros ADMINS o Roles Master
                            throw new SecurityException("Acceso denegado: Jerarquía insuficiente para crear este perfil.");
                        }
                        break;

                    case "DIREC":
                    case "GERENTE":
                        // Validación: No pueden crear ADMINS, ni CLIENTES/USUARIOS globales
                        if (rootNewName.equals("ADMIN") || rootNewName.equals("CLIENTE") || rootNewName.equals("USUARIO")) {
                            throw new SecurityException("No tienes permiso para asignar roles fuera del ámbito de empresa.");
                        }

                        // Validación: Un GERENTE no puede crear un DIRECTOR
                        if (rootName.equals("GERENTE") && rootNewName.equals("DIREC")) {
                            throw new SecurityException("Un Gerente no puede crear un perfil de rango Superior (Directivo).");
                        }

                        // Sellar el usuario a la empresa del operador
                        if (idCompnayContext != null) {
                            uEntity.setCompanyId(companyService.findById(idCompnayContext));
                        } else {
                            // Si es Directivo de Avalon Staff (SaaS), puede crear staff global
                            uEntity.setCompanyId(null);
                        }
                        break;

                    case "OPT":
                    case "CLIENTE":
                    case "USUARIO":
                        // Estos roles no tienen permitido crear a nadie por seguridad
                        throw new SecurityException("Tu rol actual no permite la creación de nuevos usuarios.");

                    default:
                        // Caso de seguridad por defecto
                        rolNewUser = masterDataService.searchByShortName("INVITADO");
                        uEntity.setCompanyId(null);
                }
            }
        } else {
            // --- REGISTRO PÚBLICO (CASO B: Usuario que entra por la Web/App solo) ---
            rolNewUser = masterDataService.searchByShortName("INVITADO");
            uEntity.setCompanyId(null); // No pertenece a ninguna empresa hasta que compre
        }

        // 2. Doble check de disponibilidad del nombre de usuario
        userRepository.findByUserName(userCreate.userName()).ifPresent(u -> {
            throw new EntityExistsException("El nombre de usuario '" + userCreate.userName() + "' ya está en uso.");
        });

        // 3. Construcción y persistencia de la entidad
        Person personEntity = personService.searchById(userCreate.personId());

        uEntity.setPerson(personEntity);
        uEntity.setRolId(rolNewUser);
        uEntity.setUserName(userCreate.userName());
        uEntity.setHashSalt(PassSecure.generateSalt());
        uEntity.setHashPassword(PassSecure.hashPassword(userCreate.password(), uEntity.getHashSalt()));
        uEntity.setStatusId(masterDataService.searchByShortName("ACT"));

        try {
            return userRepository.save(uEntity);

        } catch (Exception e) {
            // Aquí podrías agregar un log de error (e.getMessage())
            return null;
        }
    }

    @Override
    public UserAvalon clear(Long id) {

        UserAvalon ua = searchById(id);

        MasterData statusEliminado = masterDataService.searchByShortName("DEL");

        ua.setStatusId(statusEliminado);

        return userRepository.save(ua);
    }

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

}



