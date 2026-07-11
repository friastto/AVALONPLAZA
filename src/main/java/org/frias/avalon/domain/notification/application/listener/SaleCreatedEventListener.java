package org.frias.avalon.domain.notification.application.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.frias.avalon.domain.notification.application.event.SaleCreatedEvent;
import org.frias.avalon.domain.notification.application.port.EmailSenderPort;
import org.frias.avalon.domain.notification.domain.service.TicketGeneratorService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SaleCreatedEventListener {

    private final TicketGeneratorService ticketGeneratorService;
    private final EmailSenderPort emailSenderPort;

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
            
            // 2. Formatear correo
            String bodyHtml = "<html><body style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>" +
                    "<div style='max-width: 600px; margin: 0 auto; border: 1px solid #ddd; padding: 20px; border-radius: 8px;'>" +
                    "<h2 style='color: #4CAF50; text-align: center;'>Â¡Gracias por tu compra en Avalon!</h2>" +
                    "<p>Estimado/a cliente,</p>" +
                    "<p>Hemos registrado tu compra con exito. En el archivo adjunto encontraras el **Ticket de Venta** digital en formato PDF con todos los detalles de tu compra.</p>" +
                    "<table style='width: 100%; border-collapse: collapse; margin: 20px 0;'>" +
                    "<tr><td style='padding: 8px; border-bottom: 1px solid #eee;'><b>Codigo de Venta:</b></td>" +
                    "<td style='padding: 8px; border-bottom: 1px solid #eee;'>" + event.getSaleResponse().saleCode() + "</td></tr>" +
                    "<tr><td style='padding: 8px; border-bottom: 1px solid #eee;'><b>Total a Pagar:</b></td>" +
                    "<td style='padding: 8px; border-bottom: 1px solid #eee; font-weight: bold; color: #FFD700;'>$" + event.getSaleResponse().totalAmount() + "</td></tr>" +
                    "<tr><td style='padding: 8px; border-bottom: 1px solid #eee;'><b>Metodo de Pago:</b></td>" +
                    "<td style='padding: 8px; border-bottom: 1px solid #eee;'>" + event.getSaleResponse().paymentMethod().fullName() + "</td></tr>" +
                    "</table>" +
                    "<p style='font-size: 12px; color: #777;'>Por favor conserva este ticket digital para cualquier cambio o reclamacion en tu tienda local.</p>" +
                    "<br><p style='text-align: center; font-weight: bold; color: #4CAF50;'>El equipo de Avalon</p>" +
                    "</div>" +
                    "</body></html>";
            
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