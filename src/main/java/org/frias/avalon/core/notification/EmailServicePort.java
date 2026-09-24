package org.frias.avalon.core.notification;

public interface EmailServicePort {
    void sendPasswordResetPin(String to, String pin);
    void sendApplicantVerificationPin(String to, String pin);
}