package org.frias.avalon.core.notification;

public interface EmailServicePort {
    void sendPasswordResetPin(String to, String pin);
    void sendApplicantVerificationPin(String to, String pin);
    void sendCompanyRejectionEmail(String to, String applicantName, String companyName, String reason, String explanation);
    void sendCompanyApprovalEmail(String to, String applicantName, String companyName);
}