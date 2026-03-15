package org.frias.avalon.jwt.util;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class PasswordUtils {

    /**
     * Verifica si una contraseña cruda es igual a la almacenada, usando salt
     */
    public static boolean verifyPassword(String rawPassword, String salt, String hashedPassword) {
        // Concatenamos el salt con el password ingresado
        String salted = salt + rawPassword;

        // Comparamos usando BCrypt (BCrypt internamente desencripta y compara)
        return BCrypt.checkpw(salted, hashedPassword);
    }

    /**
     * Hashea una contraseña combinada con su salt
     */
    public static String hashPassword(String rawPassword, String salt) {
        // Generamos el hash del salt+password con un nuevo salt interno de BCrypt
        return BCrypt.hashpw(salt + rawPassword, BCrypt.gensalt());
    }
}

