package org.frias.avalon.infraestructure.notification;

import org.frias.avalon.core.notification.EmailServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// @Service
public class SmtpEmailServiceAdapter implements EmailServicePort {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public SmtpEmailServiceAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetPin(String to, String pin) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Tu Código de Recuperación de Contraseña - Avalon App");
            
            String emailBody = "Hola,\\n\\n"
                    + "Has solicitado restablecer tu contraseña. Usa el siguiente código para continuar:\\n\\n"
                    + "CÓDIGO: " + pin + "\\n\\n"
                    + "Este código expirará en 10 minutos.\\n\\n"
                    + "Si no solicitaste esto, por favor ignora este correo.\\n\\n"
                    + "Gracias,\\n"
                    + "El equipo de Avalon";
            
            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            // En un entorno de producción, aquí se debería loguear el error
            // pero no lanzar la excepción al usuario para no revelar si el correo existe o no.
            System.err.println("Error al enviar correo de restablecimiento: " + e.getMessage());
        }
    }

    @Override
    public void sendApplicantVerificationPin(String to, String pin) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Codigo de Verificacion de Gerente - Avalon");

            String emailBody = "Hola,\n\n"
                    + "Has solicitado validar tu identidad como gerente corporativo en Avalon. Usa el siguiente codigo:\n\n"
                    + "CODIGO: " + pin + "\n\n"
                    + "Este codigo expirara en 10 minutos.\n\n"
                    + "Si no solicitaste esto, ignora este correo.\n\n"
                    + "El equipo de Avalon";

            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error al enviar correo de verificacion de gerente: " + e.getMessage());
        }
    }

    @Override
    public void sendCompanyRejectionEmail(String to, String applicantName, String companyName, String reason, String explanation) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Actualizacion sobre tu solicitud de empresa en Avalon");

            String emailBody = "Hola " + applicantName + ",\n\n"
                    + "Gracias por tu interes en formar parte de Avalon con tu empresa " + companyName + ".\n\n"
                    + "Te informamos que tu solicitud no ha podido ser aprobada por el siguiente motivo:\n\n"
                    + "Motivo: " + reason + "\n"
                    + "Detalle: " + explanation + "\n\n"
                    + "Si deseas corregir esta informacion, puedes iniciar una nueva solicitud desde la aplicacion movil.\n\n"
                    + "Atentamente,\nEquipo de Validacion y Auditoria - Avalon";

            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error al enviar correo de rechazo de empresa: " + e.getMessage());
        }
    }

    @Override
    public void sendCompanyApprovalEmail(String to, String applicantName, String companyName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("¡Tu empresa ha sido aprobada en Avalon!");

            String emailBody = "Estimado(a) " + applicantName + ",\n\n"
                    + "Tu solicitud para la empresa " + companyName + " ha sido APROBADA exitosamente.\n\n"
                    + "Tu esquema empresarial y tienda inicial ya se encuentran activos, y tu rol de Gerente General (GERGEN) ha sido activado.\n\n"
                    + "Ya puedes ingresar a la aplicacion movil en Modo Administrador.\n\n"
                    + "Bienvenido a Avalon,\nEl equipo de Avalon";

            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error al enviar correo de aprobacion de empresa: " + e.getMessage());
        }
    }
}