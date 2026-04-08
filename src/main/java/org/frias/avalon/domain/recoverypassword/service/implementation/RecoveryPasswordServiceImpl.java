package org.frias.avalon.domain.recoverypassword.service.implementation;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.jwt.util.PassSecure;
import org.frias.avalon.domain.email.service.interfaces.EmailService;
import org.frias.avalon.domain.person.entity.Person;
import org.frias.avalon.domain.person.services.interfaces.PersonService;
import org.frias.avalon.domain.recoverypassword.repository.PasswordResetRepository;
import org.frias.avalon.domain.recoverypassword.service.entity.PasswordReset;
import org.frias.avalon.domain.recoverypassword.service.interfaces.RecoveryPasswordService;
import org.frias.avalon.domain.usergeneral.useravalon.entities.UserAvalon;
import org.frias.avalon.domain.usergeneral.useravalon.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RecoveryPasswordServiceImpl implements RecoveryPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PersonService personService;
    private final EmailService emailService;

    public RecoveryPasswordServiceImpl(UserRepository userRepository, PasswordResetRepository passwordResetRepository, PersonService personService, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.personService = personService;
        this.emailService = emailService;
    }


    @Override
    public List<String> initiateRecovery(String identification, String method) {
        // 1. Buscamos la persona por su cédula
        Person person = personService.findByNumberId(identification);


        // 2. Traemos todos sus usuarios (Invitado, Empleado, etc.)
        List<UserAvalon> users = userRepository.findAllByPersonId(person.getId());

        if (users.isEmpty()) throw new EntityNotFoundException("No tienes cuentas creadas.");

        // 3. Generamos un token único para esta sesión de recuperación
        String token = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

        // 4. Guardamos el token para CADA usuario de esa persona
        // Así, cuando el usuario elija cuál recuperar, el token servirá para ambos
        for (UserAvalon u : users) {
            PasswordReset pr = new PasswordReset();
            pr.setUserName(u.getUserName());
            pr.setToken(token);
            pr.setExpiryDate(expiry);
            passwordResetRepository.save(pr);
        }

        // 5. Envío de la notificación
        if ("EMAIL".equals(method)) {
            emailService.sendRecoveryCode(person.getEmail(), token);
        } else {
            // Aquí iría tu lógica de SMS con Twilio
            System.out.println("Enviando SMS al: " + person.getPhoneNumber() + " con el código: " + token);
        }

        // Devolvemos la lista de nombres de usuario para que el Frontend
        // le pregunte: "¿Cuál de estos usuarios quieres restablecer?"
        return users.stream().map(UserAvalon::getUserName).collect(Collectors.toList());
    }

    @Override
    public void finishRecovery(String userName, String token, String newPassword) {
// 1. Validamos el token y que pertenezca a ese name de usuario
        PasswordReset reset = passwordResetRepository.findByTokenAndUserName(token, userName)
                .orElseThrow(() -> new SecurityException("Código inválido o para el usuario incorrecto."));

        if (reset.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new SecurityException("El código ha expirado.");
        }

        // 2. Buscamos el usuario y actualizamos con PassSecure
        UserAvalon user = userRepository.findByUserName(userName).get();
        String salt = PassSecure.generateSalt();
        user.setHashSalt(salt);

        user.setHashPassword(PassSecure.hashPassword(newPassword, salt));

        userRepository.save(user);

        // 3. Limpiamos los tokens de esa persona para que no se reusen
        passwordResetRepository.deleteByUserName(userName);

    }
}
