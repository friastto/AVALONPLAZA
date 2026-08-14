package org.frias.avalon.domain.person.domain.model;

import org.frias.avalon.core.exeptions.BusinessException;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Pure Java Domain model representing a Person in ApiAvalon.
 * Free of Lombok annotations.
 */
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

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private PersonDomain() {
    }

    public static PersonDomain createBasic(Long typeIdentificationId, String numberid, String name, String lastName, String address, Long sexId, Long phoneNumber, String email, Long statusId) {
        validateRequired(numberid, "El numero de identificacion es requerido");
        validateRequired(name, "El nombre es requerido");
        validateRequired(lastName, "El apellido es requerido");

        if ((phoneNumber == null || phoneNumber <= 0) && (email == null || email.isBlank())) {
            throw new BusinessException("Se necesita al menos un telefono o un email valido para crear el registro");
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
        person.statusId = statusId;

        return person;
    }

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

    private static void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
    }

    private static void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("El formato del email no es valido");
        }
    }

    public String getFullName() {
        return String.format("%s %s", this.name, this.lastName);
    }

    public void changeStatus(Long newStatus) {
        this.statusId = newStatus;
    }

    public Long getId() { return id; }
    public String getNumberid() { return numberid; }
    public String getName() { return name; }
    public String getLastName() { return lastName; }
    public String getAddress() { return address; }
    public Long getTypeIdentificationId() { return typeIdentificationId; }
    public Long getSexId() { return sexId; }
    public Long getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public Long getStatusId() { return statusId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}