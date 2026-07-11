package org.frias.avalon.domain.notification.infrastructure.adapter;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.notification.application.port.EmailSenderPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailSenderAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;

    @Override
    public void sendEmailWithAttachment(String to, String subject, String bodyHtml, byte[] attachment, String attachmentName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(bodyHtml, true);
            
            ByteArrayResource pdfResource = new ByteArrayResource(attachment);
            helper.addAttachment(attachmentName, pdfResource);
            
            mailSender.send(message);
        } catch (Exception e) {
            throw new BusinessException("Error al enviar correo con archivo adjunto: " + e.getMessage());
        }
    }
}