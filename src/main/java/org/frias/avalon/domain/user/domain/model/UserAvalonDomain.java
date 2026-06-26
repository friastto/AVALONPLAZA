package org.frias.avalon.domain.user.domain.model;

import lombok.Getter;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.core.validation.PassSecure;

import java.time.LocalDateTime;

@Getter
public class UserAvalonDomain {
    private Long id;
    private Long personId;
    private String userName;
    private String hashSalt;
    private String hashPassword;
    private Long statusId;
    private String statusCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserAvalonDomain(Long id, String userName, Long statusId) {
        this.id = id;
        this.userName = userName;
        this.statusId = statusId;
    }

    public UserAvalonDomain(
            Long id,
            Long personId,
            String userName,
            String hashSalt,
            String hashPassword,
            Long statusId
    ) {
        this.id = id;
        this.personId = personId;
        this.userName = userName;
        this.hashSalt = hashSalt;
        this.hashPassword = hashPassword;
        this.statusId = statusId;
    }

    public UserAvalonDomain(
            String userName,
            String hashSalt,
            String hashPassword,
            Long statusId
    ) {
        this.userName = userName;
        this.hashSalt = hashSalt;
        this.hashPassword = hashPassword;
        this.statusId = statusId;
    }

    public UserAvalonDomain(Long id, Long personId, String userName, Long statusId) {
        this.id = id;
        this.personId = personId;
        this.userName = userName;
        this.hashSalt = hashSalt;
        this.hashPassword = hashPassword;
        this.statusId = statusId;
    }

    public static UserAvalonDomain create(
            String userName,
            String hashSalt,
            String hashPassword,
            Long statusId
    ) {
        return new UserAvalonDomain(
                userName,
                hashSalt,
                hashPassword,
                statusId
        );
    }

    public static UserAvalonDomain fromPersistenceBasic(Long id, Long personId, String userName, Long statusId) {
        return new UserAvalonDomain(id, personId, userName, statusId);
    }

    public static UserAvalonDomain fromPersistenceAdvanced(
            Long id,
            Long personId,
            String userName,
            String hashSalt,
            String hashPassword,
            Long statusId
    ) {
        return new UserAvalonDomain(
                id,
                personId,
                userName,
                hashSalt,
                hashPassword,
                statusId);
    }


    public boolean verifyPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank() || this.hashSalt == null || this.hashPassword == null) {
            return false;
        }

        // 1. Usar la MISMA utilidad de hasheo para recrear el hash
        //    con la contraseña proporcionada y el SALT ALMACENADO del usuario.
        String hashToVerify = PassSecure.hashPassword(rawPassword, this.hashSalt);

        // 2. Comparar el hash recién generado con el hash que ya está guardado.
        return this.hashPassword.equals(hashToVerify);
    }

    public void changePassword(String newRawPassword) {
        if (newRawPassword == null || newRawPassword.isBlank() || newRawPassword.length() < 8) {
            throw new DomainValidationException("La nueva contraseña no es válida o es demasiado corta (mínimo 8 caracteres).");
        }
        // Generar un NUEVO salt para máxima seguridad
        this.hashSalt = PassSecure.generateSalt();
        // Generar un NUEVO hash con la nueva contraseña y el nuevo salt
        this.hashPassword = PassSecure.hashPassword(newRawPassword, this.hashSalt);
    }
}