package org.frias.avalon.infraestructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.frias.avalon.core.notification.EmailServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Adapter implementation for sending email notifications using the Brevo HTTP API.
 * This class implements {@link EmailServicePort} to handle password reset pin dispatch.
 */
@Service
@Primary
@Slf4j
public class BrevoEmailServiceAdapter implements EmailServicePort {

    private final RestClient restClient;

    @Value("${brevo.api.url}")
    private String apiUrl;

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    public BrevoEmailServiceAdapter() {
        this.restClient = RestClient.create();
    }

    /**
     * Sends a password reset PIN to the specified recipient.
     *
     * @param to  the recipient's email address
     * @param pin the recovery pin code
     */
    @Override
    public void sendPasswordResetPin(String to, String pin) {
        log.info("Sending password reset PIN to {} using Brevo API", to);
        try {
            String emailBody = "<html><body>"
                    + "<p>Hola,</p>"
                    + "<p>Has solicitado restablecer tu contraseña. Usa el siguiente código para continuar:</p>"
                    + "<h3 style='color: #4CAF50;'>CÓDIGO: " + pin + "</h3>"
                    + "<p>Este código expirará en 10 minutos.</p>"
                    + "<p>Si no solicitaste esto, por favor ignora este correo.</p>"
                    + "<br><p>Gracias,<br>El equipo de Avalon</p>"
                    + "</body></html>";

            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", senderName, "email", senderEmail),
                    "to", List.of(Map.of("email", to)),
                    "subject", "Tu Código de Recuperación de Contraseña - Avalon App",
                    "htmlContent", emailBody
            );

            restClient.post()
                    .uri(apiUrl)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Password reset PIN sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset PIN to {} via Brevo: {}", to, e.getMessage(), e);
        }
    }
}
