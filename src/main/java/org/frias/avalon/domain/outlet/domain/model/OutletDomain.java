package org.frias.avalon.domain.outlet.domain.model;


import lombok.Builder;
import lombok.Getter;
import org.frias.avalon.core.exeptions.DomainValidationException;

import java.util.UUID;
import java.math.BigDecimal;


@Getter
@Builder
public class OutletDomain {

    private Long id;

    private String code;

    private String name;

    private String address;

    private String phone;

    private String nit;

    private Long statusId;

    private LocationDomain location;

    private BigDecimal cashThresholdAmount;

    public void setCashThresholdAmount(BigDecimal cashThresholdAmount) {
        this.cashThresholdAmount = cashThresholdAmount;
    }


    public static OutletDomain create(String name, String address, String phone, String nit, Long status, LocationDomain location) {
        return create(name, address, phone, nit, status, location, null);
    }

    public static OutletDomain create(String name, String address, String phone, String nit, Long status, LocationDomain location, BigDecimal cashThresholdAmount) {

        if (name.isBlank()) {
            throw new DomainValidationException("el nombre de la Tienda no pude estar vavcio");
        }
        if (address.isBlank()) {
            throw new DomainValidationException("la direccion de la tienda no puede estar vacio");
        }
        if (phone.isBlank()) {
            throw new DomainValidationException("el telefono de latienda no puede estar vacio");
        }
        if (nit.isBlank()) {
            throw new DomainValidationException("el nit de la tienda no puede estar vacio");
        }
        if (location.longitude() == null || location.latitude() == null) {
            throw new DomainValidationException("la ubicacion geografica no puede estar vacia");
        }


        return OutletDomain.builder()
                .code(generateCode())
                .name(name)
                .address(address)
                .phone(phone)
                .nit(nit)
                .statusId(status)
                .location(location)
                .cashThresholdAmount(cashThresholdAmount)
                .build();
    }

    public static OutletDomain fromPersistence(Long id, String code, String name, String address, String phone, String nit, Long status, LocationDomain location) {
        return fromPersistence(id, code, name, address, phone, nit, status, location, null);
    }

    public static OutletDomain fromPersistence(Long id, String code, String name, String address, String phone, String nit, Long status, LocationDomain location, BigDecimal cashThresholdAmount) {

        return OutletDomain.builder()
                .id(id)
                .code(code)
                .name(name)
                .address(address)
                .phone(phone)
                .nit(nit)
                .location(location)
                .statusId(status)
                .cashThresholdAmount(cashThresholdAmount)
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