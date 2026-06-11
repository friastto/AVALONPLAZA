/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.frias.avalon.core.validation;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 *
 * @author usuario
 */
public class PassSecure {
    // Algoritmo y parámetros
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16; // bytes

    // Genera un salt aleatorio
    public static String generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        sr.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // Genera el hash de la contraseña con el salt
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null || salt.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña o el salt no pueden estar vacíos");
        }
        
        try {
            byte[] decodedSalt = Base64.getDecoder().decode(salt);
            if (decodedSalt.length == 0) {
                throw new IllegalArgumentException("El salt decodificado está vacío");
            }
            
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    decodedSalt,
                    ITERATIONS,
                    KEY_LENGTH
            );
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error al hashear la contraseña", e);
        }
    }

    public static boolean verifyPassword(String password, String salt, String expectedHash) {
        try {
            if (password == null || salt == null || salt.trim().isEmpty() || expectedHash == null) {
                return false; // Credenciales inválidas de forma segura
            }
            String pwdHash = hashPassword(password, salt);
            return pwdHash.equals(expectedHash);
        } catch (Exception e) {
            return false; // falla silenciosa → no revela detalles
        }
    }
}