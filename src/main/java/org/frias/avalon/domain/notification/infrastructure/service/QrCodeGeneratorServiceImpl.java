package org.frias.avalon.domain.notification.infrastructure.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.notification.domain.service.QrCodeGeneratorService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class QrCodeGeneratorServiceImpl implements QrCodeGeneratorService {

    @Override
    public String generateQrCodeBase64(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();
            
            return Base64.getEncoder().encodeToString(pngData);
        } catch (Exception e) {
            throw new BusinessException("Error al generar el cÃ³digo QR: " + e.getMessage());
        }
    }
}