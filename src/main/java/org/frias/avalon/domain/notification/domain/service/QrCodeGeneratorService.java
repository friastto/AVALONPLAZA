package org.frias.avalon.domain.notification.domain.service;

public interface QrCodeGeneratorService {
    String generateQrCodeBase64(String text, int width, int height);
}