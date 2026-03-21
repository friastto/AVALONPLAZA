package org.frias.avalon.useravalon.services.implementation;

import jakarta.persistence.EntityExistsException;
import org.frias.avalon.empresasucursal.empresa.services.interfaces.CompanyService;
import org.frias.avalon.empresasucursal.tenant.tenantcontex.TenantContext;
import org.frias.avalon.core.jwt.util.JwtUtils;
import org.frias.avalon.maestra.entities.MasterData;
import org.frias.avalon.maestra.services.interfaces.MasterDataService;
import org.frias.avalon.person.entity.Person;
import org.frias.avalon.person.services.interfaces.PersonService;
import org.frias.avalon.useravalon.dtos.UserLinkPersonRequestDto;
import org.frias.avalon.useravalon.dtos.UserRequestNewDto;
import org.frias.avalon.useravalon.dtos.UserValidateCredentials;
import org.frias.avalon.useravalon.entities.UserAvalon;
import org.frias.avalon.useravalon.repositories.UserRepository;
import org.frias.avalon.useravalon.services.interfaces.UsuarioService;
import org.frias.avalon.useravalon.services.interfaces.UsuarioServiceValidate;
import org.frias.avalon.util.PassSecure;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService, UsuarioServiceValidate {


    private final UserRepository userRepository;
    private final PersonService personService;
    private final MasterDataService masterDataService;
    private final JwtUtils jwtUtils;
    private final CompanyService companyService;

    public UsuarioServiceImpl(UserRepository userRepository, PersonService personService, MasterDataService masterDataService, JwtUtils jwtUtils, CompanyService companyService) {
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

    @Transactional
    @Override
    public Boolean saveUserAndPerson(UserRequestNewDto userCreate) {

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
    public Boolean saveUserAndCreateLinkPerson(UserLinkPersonRequestDto userCreate) {

        UserAvalon uEntity = new UserAvalon();

        MasterData rolNewUser;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        /*System.out.println("--- ANÁLISIS DE EMERGENCIA ---");
        System.out.println("¿Es null?: " + (auth == null));
        System.out.println("Nombre: " + (auth != null ? auth.getName() : "N/A"));
        System.out.println("Clase: " + (auth != null ? auth.getClass().getSimpleName() : "N/A"));
        System.out.println("Credenciales presentes: " + (auth != null && auth.getCredentials() != null));


         */
        boolean isAnonimous = auth == null ||
                !auth.isAuthenticated() ||
                auth instanceof AnonymousAuthenticationToken;

        //System.out.println("**************************\n DEBUG: el suaurio es anonimo ?  " + isAnonimous +"\n *********************");
if (!isAnonimous) {
    String token = (String) auth.getCredentials();


    // 1. Obtener el rol en el contexto de seguridad  del usuario que está operando
    String operador = jwtUtils.extractRol(token);

    System.out.println("\n\n********************************************************\n " +
                       "\tel rol del Usuario actual es " + operador +
                       "\n********************************************************\n\n");

    if (operador != null) {
        MasterData rolUserAvalon = masterDataService.searchShortName(operador);

        MasterData rolUserAvalonIs = masterDataService.getRootBranch(rolUserAvalon.getId(), "ROL");


        // 2. Ahora puedes usar el objeto devuelto para tomar decisiones
        switch (rolUserAvalonIs.getShortName()) {
            case "ADMIN":
                // Lógica para personal de AVALON (SaaS)

                rolNewUser = masterDataService.findById(userCreate.roleId());

                break;
            case "DIREC":
                rolNewUser = masterDataService.findById(userCreate.roleId());
                MasterData validateRolNewUser1 = masterDataService.getRootBranch(rolNewUser.getId(), "ROL");

                if (validateRolNewUser1.getShortName().equals("ADMIN") ||
                        validateRolNewUser1.getShortName().equals("CLIENTE") ||
                        validateRolNewUser1.getShortName().equals("USUARIO") ||
                        rolNewUser.getShortName().equals(operador) )
                    throw new SecurityException("error en la asignacion del rol no cumples con los parametros.");

                Long idEmpresa1 = TenantContext.getTenantId();


                if (idEmpresa1 != null) {
                    // Lógica para altos mandos de la Empresa
                    uEntity.setCompanyId(companyService.findById(idEmpresa1));
                }



                break;
            case "GERENTE" :

                rolNewUser = masterDataService.findById(userCreate.roleId());
                MasterData validateRolNewUser = masterDataService.getRootBranch(rolNewUser.getId(), "ROL");

                if (validateRolNewUser.getShortName().equals("ADMIN") ||
                    validateRolNewUser.getShortName().equals("DIREC") ||
                    validateRolNewUser.getShortName().equals("CLIENTE") ||
                    validateRolNewUser.getShortName().equals("USUARIO") ||
                    rolNewUser.getShortName().equals(operador) )
                    throw new SecurityException("error en la asignacion del rol no cumples con los parametros.");


                Long idEmpresa = TenantContext.getTenantId();

                if (idEmpresa != null) {
                    // Lógica para altos mandos de la Empresa
                    uEntity.setCompanyId(companyService.findById(idEmpresa));
                }

                break;

            case "OPT":
                // Lógica para personal de campo (Cajeros, etc.)
                // Quizás aquí no permitas que creen otros usuarios
                throw new SecurityException("El personal operativo no puede crear usuarios.");
            case "USUARIO":
                // Lógica para personal de campo (Cajeros, etc.)
                // Quizás aquí no permitas que creen otros usuarios
                throw new SecurityException("No tiene los permisos necesarios para realizar esta accion");

            default:

                rolNewUser = masterDataService.searchShortName("INVITADO");

        }
    } else {
        rolNewUser = masterDataService.searchShortName("INVITADO");
    }
}else {
    rolNewUser = masterDataService.searchShortName("INVITADO");
}

        userRepository.findByUserName(userCreate.userName())
                .ifPresent(u -> {
                    throw new EntityExistsException("el nombre de usuario no esta disponible");
                });

        Person personEntity = personService.searchById(userCreate.personId());

        uEntity.setPerson(personEntity);

        uEntity.setRolId(rolNewUser);

        uEntity.setUserName(userCreate.userName());

        uEntity.setHashSalt(PassSecure.generateSalt());

        uEntity.setHashPassword(PassSecure.hashPassword(userCreate.password(), uEntity.getHashSalt()));

        uEntity.setStatusId(masterDataService.searchShortName("ACT"));

        try {
            userRepository.save(uEntity);

        } catch (Exception e) {
            return false;
        }

        return true;


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



