package org.frias.avalon.domain.notification.application.port;

public interface EmailSenderPort {
    void sendEmailWithAttachment(String to, String subject, String bodyHtml, byte[] attachment, String attachmentName);
}