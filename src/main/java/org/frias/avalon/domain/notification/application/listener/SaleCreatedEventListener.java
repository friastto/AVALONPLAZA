package org.frias.avalon.domain.notification.application.listener;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.frias.avalon.domain.notification.application.event.SaleCreatedEvent;
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
public class SaleCreatedEventListener {

    private final TicketGeneratorService ticketGeneratorService;
    private final EmailSenderPort emailSenderPort;
    private final PebbleEngine pebbleEngine;

    @EventListener
    @Async
    public void handleSaleCreated(SaleCreatedEvent event) {
        String email = event.getClientEmail();
        if (email == null || email.trim().isEmpty()) {
            log.info("Venta registrada. No se envia ticket por correo porque el cliente no tiene correo asociado.");
            return;
        }

        log.info("Iniciando proceso asincrono de envio de ticket en PDF por correo a: {}", email);
        try {
            // 1. Generar PDF
            byte[] pdfBytes = ticketGeneratorService.generateTicketPdf(event.getSaleResponse());
            
            // 2. Formatear correo usando plantilla Pebble en ASCII plano
            Map<String, Object> context = new HashMap<>();
            context.put("sale", event.getSaleResponse());
            PebbleTemplate compiledTemplate = pebbleEngine.getTemplate("sale_email");
            Writer writer = new StringWriter();
            compiledTemplate.evaluate(writer, context);
            String bodyHtml = writer.toString();
            
            String subject = "Ticket de Venta - Avalon (Cod: " + event.getSaleResponse().saleCode().toString().substring(0, 8) + ")";
            String attachmentName = "Ticket_Avalon_" + event.getSaleResponse().saleCode().toString().substring(0, 8) + ".pdf";

            // 3. Enviar correo
            emailSenderPort.sendEmailWithAttachment(email, subject, bodyHtml, pdfBytes, attachmentName);
            log.info("Ticket de compra enviado exitosamente por correo a: {}", email);
        } catch (Exception e) {
            log.error("Error al generar o enviar el ticket de venta por correo: {}", e.getMessage(), e);
        }
    }
}