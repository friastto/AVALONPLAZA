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

    /**
     * Sends an applicant verification PIN for company registration to the specified recipient.
     *
     * @param to  the recipient's email address
     * @param pin the 6-digit verification PIN
     */
    @Override
    public void sendApplicantVerificationPin(String to, String pin) {
        log.info("Sending applicant verification PIN to {} using Brevo API", to);
        try {
            String emailBody = "<html><body>"
                    + "<p>Hola,</p>"
                    + "<p>Has sido postulado o has solicitado registrar una empresa en Avalon. Para validar tu identidad como gerente corporativo, ingresa el siguiente codigo de seguridad:</p>"
                    + "<h2 style='color: #00FF7F; letter-spacing: 4px;'>CODIGO: " + pin + "</h2>"
                    + "<p>Este codigo de verificacion expirara en 10 minutos.</p>"
                    + "<p>Si no realizaste esta solicitud, por favor ignora este correo.</p>"
                    + "<br><p>Atentamente,<br>Equipo de Seguridad Avalon</p>"
                    + "</body></html>";

            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", senderName, "email", senderEmail),
                    "to", List.of(Map.of("email", to)),
                    "subject", "Codigo de Verificacion de Gerente - Avalon",
                    "htmlContent", emailBody
            );

            restClient.post()
                    .uri(apiUrl)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Applicant verification PIN sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send applicant verification PIN to {} via Brevo: {}", to, e.getMessage(), e);
        }
    }

    @Override
    public void sendCompanyRejectionEmail(String to, String applicantName, String companyName, String reason, String explanation) {
        log.info("Sending company rejection email to {} for company {} using Brevo API", to, companyName);
        try {
            String emailBody = "<html><body style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>"
                    + "<p>Hola " + applicantName + ",</p>"
                    + "<p>Gracias por tu interes en formar parte de la plataforma comercial Avalon con tu empresa <strong>" + companyName + "</strong>.</p>"
                    + "<p>Tras revisar minuciosamente la informacion remitida, te informamos que en esta oportunidad tu solicitud no ha podido ser aprobada por el siguiente motivo:</p>"
                    + "<div style='background-color: #fff2f2; border-left: 4px solid #ff4d4f; padding: 12px 16px; margin: 16px 0; border-radius: 4px;'>"
                    + "<p style='margin: 0 0 8px 0; font-weight: bold; color: #cf1322;'>Motivo: " + reason + "</p>"
                    + "<p style='margin: 0; color: #595959;'>" + explanation + "</p>"
                    + "</div>"
                    + "<p>Si consideras que la documentacion o los datos pueden ser subsanados, te invitamos a iniciar una nueva solicitud de servicio desde la aplicacion movil de Avalon adjuntando la informacion corregida.</p>"
                    + "<br><p>Atentamente,<br><strong>Equipo de Validacion y Auditoria - Avalon</strong></p>"
                    + "</body></html>";

            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", senderName, "email", senderEmail),
                    "to", List.of(Map.of("email", to)),
                    "subject", "Actualizacion sobre tu solicitud de empresa en Avalon",
                    "htmlContent", emailBody
            );

            restClient.post()
                    .uri(apiUrl)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Company rejection email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send company rejection email to {} via Brevo: {}", to, e.getMessage(), e);
        }
    }

    @Override
    public void sendCompanyApprovalEmail(String to, String applicantName, String companyName) {
        log.info("Sending company approval email to {} for company {} using Brevo API", to, companyName);
        try {
            String emailBody = "<html><body style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>"
                    + "<p>Estimado(a) " + applicantName + ",</p>"
                    + "<p>Nos complace informarte que tu solicitud para la empresa <strong>" + companyName + "</strong> ha sido <strong>APROBADA</strong> por el equipo de Avalon.</p>"
                    + "<p>Tu esquema empresarial dedicado y tu tienda inicial ya se encuentran activos en la plataforma. Tu usuario ha sido promovido al rol de <strong>Gerente General (GERGEN)</strong>.</p>"
                    + "<div style='background-color: #f6ffed; border-left: 4px solid #52c41a; padding: 12px 16px; margin: 16px 0; border-radius: 4px;'>"
                    + "<p style='margin: 0; font-weight: bold; color: #389e0d;'>¡Todo listo para operar!</p>"
                    + "<p style='margin: 4px 0 0 0; color: #595959;'>Abre la aplicacion movil de Avalon, ingresa con tu cuenta y alterna al <strong>Modo Administrador</strong> para configurar tus productos, tiendas y personal.</p>"
                    + "</div>"
                    + "<br><p>Bienvenido a Avalon,<br><strong>El equipo de Avalon</strong></p>"
                    + "</body></html>";

            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", senderName, "email", senderEmail),
                    "to", List.of(Map.of("email", to)),
                    "subject", "¡Tu empresa ha sido aprobada en Avalon!",
                    "htmlContent", emailBody
            );

            restClient.post()
                    .uri(apiUrl)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Company approval email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send company approval email to {} via Brevo: {}", to, e.getMessage(), e);
        }
    }
}

