package org.frias.avalon.core.uploadimg.removebg;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class RemoveBgService {


    @Value("${removebg.apikey}")
    private String apiKey;


    public byte[] removeBackground(byte[] file) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadFile = new HttpPost("https://api.remove.bg/v1.0/removebg");
            uploadFile.addHeader("X-Api-Key", apiKey);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("image_file", file, ContentType.DEFAULT_BINARY, "image.bin");
            builder.addTextBody("size", "auto"); // 'auto' para optimizar créditos

            uploadFile.setEntity(builder.build());

            return httpClient.execute(uploadFile, response -> {
                if (response.getCode() == 200) {
                    return EntityUtils.toByteArray(response.getEntity());
                } else {
                    throw new RuntimeException("Error en Remove.bg: " + response.getReasonPhrase());
                }
            });
        }catch (IOException ex){
            ex.printStackTrace();
            throw new RuntimeException("Error en Remove.bg: " + ex.getMessage());
        }
    }
}

