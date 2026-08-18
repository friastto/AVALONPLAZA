package org.frias.avalon.domain.outlet.domain.model;

import org.frias.avalon.core.exeptions.DomainValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pure Java Domain model representing an Outlet in ApiAvalon.
 * Free of third-party framework annotations (no Lombok, no Spring, no JPA).
 */
public class OutletDomain {

    private final Long id;
    private String code;
    private final String name;
    private final String address;
    private final String phone;
    private final String nit;
    private final Long statusId;
    private final LocationDomain location;
    private BigDecimal cashThresholdAmount;
    private Boolean deliveryEnabled;
    private BigDecimal deliveryFee;
    private final Long companyId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public OutletDomain(
            Long id,
            String code,
            String name,
            String address,
            String phone,
            String nit,
            Long statusId,
            LocationDomain location,
            BigDecimal cashThresholdAmount,
            Boolean deliveryEnabled,
            BigDecimal deliveryFee,
            Long companyId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.nit = nit;
        this.statusId = statusId;
        this.location = location;
        this.cashThresholdAmount = cashThresholdAmount;
        this.deliveryEnabled = deliveryEnabled != null ? deliveryEnabled : false;
        this.deliveryFee = deliveryFee != null ? deliveryFee : BigDecimal.ZERO;
        this.companyId = companyId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static OutletDomain create(String name, String address, String phone, String nit, Long status, LocationDomain location) {
        return create(name, address, phone, nit, status, location, null, null);
    }

    public static OutletDomain create(String name, String address, String phone, String nit, Long status, LocationDomain location, BigDecimal cashThresholdAmount) {
        return create(name, address, phone, nit, status, location, cashThresholdAmount, null);
    }

    public static OutletDomain create(String name, String address, String phone, String nit, Long status, LocationDomain location, BigDecimal cashThresholdAmount, Long companyId) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("El nombre de la tienda no puede estar vacio");
        }
        if (address == null || address.isBlank()) {
            throw new DomainValidationException("La direccion de la tienda no puede estar vacio");
        }
        if (phone == null || phone.isBlank()) {
            throw new DomainValidationException("El telefono de la tienda no puede estar vacio");
        }
        if (nit == null || nit.isBlank()) {
            throw new DomainValidationException("El NIT de la tienda no puede estar vacio");
        }
        if (location == null || location.longitude() == null || location.latitude() == null) {
            throw new DomainValidationException("La ubicacion geografica no puede estar vacia");
        }

        return new OutletDomain(
                null,
                generateCode(),
                name,
                address,
                phone,
                nit,
                status,
                location,
                cashThresholdAmount,
                false,
                BigDecimal.ZERO,
                companyId,
                LocalDateTime.now(),
                null
        );
    }

    public static OutletDomain fromPersistence(Long id, String code, String name, String address, String phone, String nit, Long status, LocationDomain location) {
        return fromPersistence(id, code, name, address, phone, nit, status, location, null, false, BigDecimal.ZERO, null, null, null);
    }

    public static OutletDomain fromPersistence(Long id, String code, String name, String address, String phone, String nit, Long status, LocationDomain location, BigDecimal cashThresholdAmount) {
        return fromPersistence(id, code, name, address, phone, nit, status, location, cashThresholdAmount, false, BigDecimal.ZERO, null, null, null);
    }

    public static OutletDomain fromPersistence(Long id, String code, String name, String address, String phone, String nit, Long status, LocationDomain location, BigDecimal cashThresholdAmount, Boolean deliveryEnabled, BigDecimal deliveryFee, Long companyId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new OutletDomain(
                id,
                code,
                name,
                address,
                phone,
                nit,
                status,
                location,
                cashThresholdAmount,
                deliveryEnabled,
                deliveryFee,
                companyId,
                createdAt,
                updatedAt
        );
    }

    public void setCashThresholdAmount(BigDecimal cashThresholdAmount) {
        this.cashThresholdAmount = cashThresholdAmount;
    }

    public void setDeliveryEnabled(Boolean deliveryEnabled) {
        this.deliveryEnabled = deliveryEnabled != null ? deliveryEnabled : false;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee != null ? deliveryFee : BigDecimal.ZERO;
    }

    public void codeGenerator() {
        if (this.code == null || this.code.isBlank()) {
            this.code = generateCode();
        }
    }

    private static String generateCode() {
        return "Oult-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public boolean isActive(String status) {
        return "ACT".equals(status);
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getNit() { return nit; }
    public Long getStatusId() { return statusId; }
    public LocationDomain getLocation() { return location; }
    public BigDecimal getCashThresholdAmount() { return cashThresholdAmount; }
    public Boolean getDeliveryEnabled() { return deliveryEnabled; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public Long getCompanyId() { return companyId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}