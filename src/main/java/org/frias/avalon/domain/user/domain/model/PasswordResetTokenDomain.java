package org.frias.avalon.domain.user.domain.model;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

public class PasswordResetTokenDomain {
    private Long id;
    private String pin;
    private String verificationToken;
    private Long userId;
    private LocalDateTime expiryDate;

    private static final int EXPIRATION_MINUTES = 10;
    private static final int PIN_LENGTH = 6;

    public PasswordResetTokenDomain(Long id, String pin, String verificationToken, Long userId, LocalDateTime expiryDate) {
        this.id = id;
        this.pin = pin;
        this.verificationToken = verificationToken;
        this.userId = userId;
        this.expiryDate = expiryDate;
    }

    public static PasswordResetTokenDomain create(Long userId) {
        String pin = generatePin();
        String verificationToken = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
        return new PasswordResetTokenDomain(null, pin, verificationToken, userId, expiryDate);
    }

    private static String generatePin() {
        SecureRandom random = new SecureRandom();
        StringBuilder pin = new StringBuilder(PIN_LENGTH);
        for (int i = 0; i < PIN_LENGTH; i++) {
            pin.append(random.nextInt(10));
        }
        return pin.toString();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    // Getters
    public Long getId() { return id; }
    public String getPin() { return pin; }
    public String getVerificationToken() { return verificationToken; }
    public Long getUserId() { return userId; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
}