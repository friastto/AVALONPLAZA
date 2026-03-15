package org.frias.avalon.useravalon.services.implementation;

import jakarta.persistence.EntityExistsException;
import org.frias.avalon.maestra.entities.MasterData;
import org.frias.avalon.maestra.services.interfaces.MasterDataService;
import org.frias.avalon.person.entity.Person;
import org.frias.avalon.person.services.interfaces.PersonService;
import org.frias.avalon.useravalon.dtos.UserLinkPersonRequestDto;
import org.frias.avalon.useravalon.dtos.UserRequestNewDto;
import org.frias.avalon.useravalon.dtos.UserResponseDto;
import org.frias.avalon.useravalon.dtos.UserValidateCredentials;
import org.frias.avalon.useravalon.entities.UserAvalon;
import org.frias.avalon.useravalon.repositories.UserRepository;
import org.frias.avalon.useravalon.services.interfaces.UsuarioService;
import org.frias.avalon.useravalon.services.interfaces.UsuarioServiceValidate;
import org.frias.avalon.util.PassSecure;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService, UsuarioServiceValidate {


    private final UserRepository userRepository;
    private final PersonService personService;
    private final MasterDataService masterDataService;

    public UsuarioServiceImpl(UserRepository userRepository, PersonService personService, MasterDataService masterDataService) {
        this.userRepository = userRepository;
        this.personService = personService;
        this.masterDataService = masterDataService;
    }


    @Override
    public UserAvalon searchByUserName(String userName) {


        return userRepository.findByUserName(userName)
                .orElseThrow( );

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
    public UserResponseDto saveUserAndPerson(UserRequestNewDto userCreate) {

            // se construye la entidad Usuario

            userRepository.findByUserName(userCreate.userName()).ifPresent(u -> {throw  new EntityExistsException("el nombre de usuario no esta disponible");});

            Person personEntity = personService.save(userCreate.personId());

            MasterData rol = masterDataService.searchShortName("USANONIMO");

            try {

                UserAvalon uEntity = new UserAvalon();

                uEntity.setPerson(personEntity);

                uEntity.setRolId(rol);

                uEntity.setUserName(userCreate.userName());

                uEntity.setHashSalt(PassSecure.generateSalt());
                uEntity.setHashPassword(PassSecure.hashPassword(userCreate.password(),uEntity.getHashSalt()));
                uEntity.setStatusId(masterDataService.searchShortName("ACT"));

                UserAvalon uCreated = userRepository.save(uEntity);

                return new UserResponseDto(
                        uCreated.getId(),
                        uCreated.getUserName(),
                        uCreated.getRolId().getFullName(),
                        0L

                );

            } catch (DataIntegrityViolationException e) {
                throw new DataIntegrityViolationException("Error al guardar usuario: " + e.getRootCause().getMessage());
            } catch (InvalidKeySpecException e) {
                throw new RuntimeException("error al guardar usuario: " + e.getMessage());
            }
        }

    @Override
    public UserResponseDto saveUserAndCreateLinkPerson(UserLinkPersonRequestDto userCreate) throws InvalidKeySpecException {

        userRepository.findByUserName(userCreate.userName()).ifPresent(u -> {throw  new EntityExistsException("el nombre de usuario no esta disponible");});

        Person personEntity = personService.searchById(userCreate.personId());

        MasterData rol = masterDataService.searchShortName("ADMINTI");



            UserAvalon uEntity = new UserAvalon();

            uEntity.setPerson(personEntity);

            uEntity.setRolId(rol);

            uEntity.setUserName(userCreate.userName());

            uEntity.setHashSalt(PassSecure.generateSalt());
            uEntity.setHashPassword(PassSecure.hashPassword(userCreate.password(),uEntity.getHashSalt()));
            uEntity.setStatusId(masterDataService.searchShortName("ACT"));

            UserAvalon uCreated = userRepository.save(uEntity);

            return new UserResponseDto(
                    uCreated.getId(),
                    uCreated.getUserName(),
                    uCreated.getRolId().getFullName(),
                    0L

            );


    }


    @Override
    public Boolean validateUser(UserValidateCredentials userValidateCredentials) {

        UserAvalon user = userRepository.findByUserName(userValidateCredentials.userName())
                .orElseThrow(()->{throw new RuntimeException("credenciales invalidas");});

        return PassSecure.verifyPassword(userValidateCredentials.password(), user.getHashSalt(),user.getHashPassword());

    }
}



