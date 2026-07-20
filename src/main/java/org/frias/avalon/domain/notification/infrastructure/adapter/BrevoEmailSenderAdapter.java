package org.frias.avalon.domain.notification.infrastructure.adapter;

import lombok.extern.slf4j.Slf4j;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.notification.application.port.EmailSenderPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Adapter implementation for sending transactional emails with attachments using the Brevo HTTP API.
 * This class implements {@link EmailSenderPort} to handle pdf ticket dispatch.
 */
@Component
@Primary
@Slf4j
public class BrevoEmailSenderAdapter implements EmailSenderPort {

    private final RestClient restClient;

    @Value("${brevo.api.url}")
    private String apiUrl;

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    public BrevoEmailSenderAdapter() {
        this.restClient = RestClient.create();
    }

    /**
     * Sends an email with a base64 encoded PDF attachment.
     *
     * @param to             the recipient's email address
     * @param subject        the subject of the email
     * @param bodyHtml       the body content in HTML format
     * @param attachment     the byte array of the file attachment
     * @param attachmentName the filename of the attachment
     */
    @Override
    public void sendEmailWithAttachment(String to, String subject, String bodyHtml, byte[] attachment, String attachmentName) {
        log.info("Sending email with attachment {} to {} using Brevo API", attachmentName, to);
        try {
            String encodedAttachment = Base64.getEncoder().encodeToString(attachment);

            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", senderName, "email", senderEmail),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "htmlContent", bodyHtml,
                    "attachment", List.of(
                            Map.of(
                                    "name", attachmentName,
                                    "content", encodedAttachment
                            )
                    )
            );

            restClient.post()
                    .uri(apiUrl)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Email with attachment sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email with attachment to {} via Brevo: {}", to, e.getMessage(), e);
            throw new BusinessException("Error al enviar correo con archivo adjunto: " + e.getMessage());
        }
    }
}
