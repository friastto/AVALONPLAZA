package org.frias.avalon.domain.notification.infrastructure.service;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.notification.domain.service.QrCodeGeneratorService;
import org.frias.avalon.domain.notification.domain.service.TicketGeneratorService;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TicketGeneratorServiceImpl implements TicketGeneratorService {

    private final PebbleEngine pebbleEngine;
    private final QrCodeGeneratorService qrCodeGeneratorService;

    @Override
    public byte[] generateTicketPdf(SaleResponse sale) {
        try {
            // 1. Generar la URL de consulta o informacion del QR (Simulado para tiendas de barrio)
            String qrUrl = "https://avalon.friascorporations.org/sales/" + sale.saleCode();
            String qrBase64 = qrCodeGeneratorService.generateQrCodeBase64(qrUrl, 150, 150);

            // 2. Leer la imagen del logo en bytes y codificarla a Base64
            byte[] logoBytes = new byte[0];
            try (InputStream logoStream = getClass().getResourceAsStream("/images/logo.png")) {
                if (logoStream != null) {
                    logoBytes = logoStream.readAllBytes();
                }
            }
            String logoBase64 = Base64.getEncoder().encodeToString(logoBytes);

            // 3. Preparar el contexto de Pebble
            Map<String, Object> context = new HashMap<>();
            context.put("sale", sale);
            context.put("qrCodeBase64", qrBase64);
            context.put("logoBase64", logoBase64);

            // 4. Renderizar el HTML con Pebble
            PebbleTemplate compiledTemplate = pebbleEngine.getTemplate("ticket");
            Writer writer = new StringWriter();
            compiledTemplate.evaluate(writer, context);
            String htmlContent = writer.toString();

            // 5. Convertir HTML a PDF con OpenHTMLtoPDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, "/");
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Error al generar el PDF del ticket: " + e.getMessage());
        }
    }
}