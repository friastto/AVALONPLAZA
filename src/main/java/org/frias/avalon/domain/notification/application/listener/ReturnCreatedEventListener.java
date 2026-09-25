package org.frias.avalon.domain.notification.application.listener;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.frias.avalon.domain.notification.application.event.ReturnCreatedEvent;
import org.frias.avalon.domain.notification.application.port.EmailSenderPort;
import org.frias.avalon.domain.notification.domain.service.TicketGeneratorService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReturnCreatedEventListener {

    private final TicketGeneratorService ticketGeneratorService;
    private final EmailSenderPort emailSenderPort;
    private final PebbleEngine pebbleEngine;

    @EventListener
    @Async
    public void handleReturnCreated(ReturnCreatedEvent event) {
        String email = event.getClientEmail();
        if (email == null || email.trim().isEmpty()) {
            log.info("Devolucion registrada. No se envia comprobante por correo porque el cliente no tiene correo asociado.");
            return;
        }

        log.info("Iniciando proceso asincrono de envio de comprobante de devolucion por correo a: {}", email);
        try {
            // 1. Generar PDF
            byte[] pdfBytes = ticketGeneratorService.generateReturnTicketPdf(event.getReturnResponse());

            // 2. Formatear correo usando plantilla Pebble en ASCII plano
            Map<String, Object> context = new HashMap<>();
            context.put("returnData", event.getReturnResponse());
            PebbleTemplate compiledTemplate = pebbleEngine.getTemplate("return_email");
            Writer writer = new StringWriter();
            compiledTemplate.evaluate(writer, context);
            String bodyHtml = writer.toString();

            String shortCode = event.getReturnResponse().returnCode() != null
                    ? event.getReturnResponse().returnCode().toString().substring(0, 8)
                    : "DEV";
            String subject = "Comprobante de Devolucion - Avalon (Cod: " + shortCode + ")";
            String attachmentName = "Comprobante_Devolucion_" + shortCode + ".pdf";

            // 3. Enviar correo con adjunto PDF
            emailSenderPort.sendEmailWithAttachment(email, subject, bodyHtml, pdfBytes, attachmentName);
            log.info("Comprobante de devolucion enviado exitosamente por correo a: {}", email);
        } catch (Exception e) {
            log.error("Error al generar o enviar el comprobante de devolucion por correo: {}", e.getMessage(), e);
        }
    }
}
