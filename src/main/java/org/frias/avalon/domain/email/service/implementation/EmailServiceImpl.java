package org.frias.avalon.domain.email.service.implementation;

import org.frias.avalon.domain.email.service.interfaces.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {


    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendRecoveryCode(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Recuperación de Contraseña - Avalon SaaS");

            String contenido = "Hola,\n\n" +
                    "Has solicitado restablecer tu contraseña en Avalon.\n" +
                    "Tu código de seguridad es: " + token + "\n\n" +
                    "Este código expirará en 15 minutos.\n" +
                    "Si no solicitaste este cambio, ignora este correo.\n\n" +
                    "Atentamente,\n" +
                    "Equipo de Soporte Avalon";

            message.setText(contenido);

            mailSender.send(message);

        } catch (Exception e) {
            // Log de error para que sepas qué falló (ej. SMTP caído)
            System.err.println("Error enviando correo a " + to + ": " + e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo de recuperación.");
        }
    }
}