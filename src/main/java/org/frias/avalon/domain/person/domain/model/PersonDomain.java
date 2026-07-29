package org.frias.avalon.domain.person.domain.model;


import lombok.Getter;
import org.frias.avalon.core.exeptions.BusinessException;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Getter
public class PersonDomain {

    private Long id;
    private String numberid;
    private String name;
    private String lastName;
    private String address;
    private Long typeIdentificationId;
    private Long sexId;
    private Long phoneNumber;
    private String email;
    private Long statusId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Regex simple para validación de email
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    // Constructor privado: Nadie puede hacer 'new' desde fuera, obligando a usar los Factory Methods
    private PersonDomain() {
    }

    /**
     * createBasic: Utilizado para la creación inicial (Negocio).
     * Aquí aplicamos reglas estrictas de lo que es "obligatorio" para que una persona exista.
     */
    public static PersonDomain createBasic(Long typeIdentificationId, String numberid, String name, String lastName, String address, Long sexId, Long phoneNumber, String email, Long statusId) {
        validateRequired(numberid, "El número de identificación es requerido");
        validateRequired(name, "El nombre es requerido");
        validateRequired(lastName, "El apellido es requerido");

        // Regla: Debe tener al menos una forma de contacto
        if ((phoneNumber == null || phoneNumber <= 0) && (email == null || email.isBlank())) {
            throw new BusinessException("Se necesita al menos un teléfono o un email válido para crear el registro");
        }

        if (email != null && !email.isBlank()) {
            validateEmail(email);
        }

        PersonDomain person = new PersonDomain();
        person.typeIdentificationId = typeIdentificationId;
        person.numberid = numberid.trim();
        person.name = name.trim().toUpperCase();
        person.lastName = lastName.trim().toUpperCase();
        person.address = address != null ? address.trim() : null;
        person.sexId = sexId;
        person.phoneNumber = phoneNumber;
        person.email = email != null ? email.toLowerCase().trim() : null;

        // Valores por defecto para nueva creación
        person.statusId = statusId; // Ejemplo: Estado Activo


        return person;
    }

    /**
     * createFromEntity: Utilizado para reconstruir el objeto desde la BD.
     * No aplicamos reglas de negocio (como el contacto obligatorio), ya que el dato ya es histórico.
     */
    public static PersonDomain createFromEntity(
            Long id, String numberid, String name, String lastName, String address,
            Long typeIdentificationId, Long sexId, Long phoneNumber, String email,
            Long statusId, LocalDateTime createdAt, LocalDateTime updatedAt) {

        PersonDomain person = new PersonDomain();
        person.id = id;
        person.numberid = numberid;
        person.name = name;
        person.lastName = lastName;
        person.address = address;
        person.typeIdentificationId = typeIdentificationId;
        person.sexId = sexId;
        person.phoneNumber = phoneNumber;
        person.email = email;
        person.statusId = statusId;
        person.createdAt = createdAt;
        person.updatedAt = updatedAt;

        return person;
    }

    // --- Helpers de Validación Privados ---

    private static void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
    }

    private static void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("El formato del email no es válido");
        }
    }

    // Comportamiento: Método de utilidad de dominio
    public String getFullName() {
        return String.format("%s %s", this.name, this.lastName);
    }

    public void changeStatus(Long newStatus) {
        this.statusId = newStatus;

    }
}