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
}