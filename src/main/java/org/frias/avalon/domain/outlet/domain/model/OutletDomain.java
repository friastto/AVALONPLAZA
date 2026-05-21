package org.frias.avalon.domain.outlet.domain.model;


import lombok.Builder;
import lombok.Getter;
import org.frias.avalon.core.exeptions.DomainValidationException;

import java.util.UUID;


@Getter
@Builder
public class OutletDomain {

    private Long id;

    private String code;

    private String name;

    private String address;

    private String phone;

    private Long statusId;

    private LocationDomain location;


    public static OutletDomain create(String name, String address, String phone, Long status, LocationDomain location) {

        if (name.isBlank()) {
            throw new DomainValidationException("el nombre de la Tienda no pude estar vavcio");
        }
        if (address.isBlank()) {
            throw new DomainValidationException("la direccion de la tienda no puede estar vacio");
        }
        if (phone.isBlank()) {
            throw new DomainValidationException("el telefono de latienda no puede estar vacio");
        }
        if (location.longitude() == null || location.latitude() == null) {
            throw new DomainValidationException("la ubicacion geografica no puede estar vacia");
        }


        return OutletDomain.builder()
                .code(generateCode())
                .name(name)
                .address(address)
                .phone(phone)
                .statusId(status)
                .location(location)
                .build();
    }

    public static OutletDomain fromPersistence(Long id, String code, String name, String address, String phone, Long status, LocationDomain location) {

        return OutletDomain.builder()
                .id(id)
                .code(code)
                .name(name)
                .address(address)
                .phone(phone)
                .location(location)
                .statusId(status)
                .build();
    }

    public void codeGenerator() {
        if (this.code == null || this.code.isBlank()) {
            this.code = generateCode();
        }
    }

    private static String generateCode() {
        // Podrías usar un UUID corto o una lógica más personalizada
        return "Oult-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public boolean isActive(String status) {

        if ("ACT".equals(status)) {
            return true;
        }

        return false;

    }


}
