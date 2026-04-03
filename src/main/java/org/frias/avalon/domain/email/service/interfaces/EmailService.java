package org.frias.avalon.domain.email.service.interfaces;

public interface EmailService {
    void sendRecoveryCode(String to, String token);
}