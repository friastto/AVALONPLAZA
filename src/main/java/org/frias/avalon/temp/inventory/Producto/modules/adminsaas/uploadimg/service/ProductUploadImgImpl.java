package org.frias.avalon.temp.inventory.Producto.modules.adminsaas.uploadimg.service;

import org.frias.avalon.temp.inventory.Producto.modules.adminsaas.uploadimg.removebg.RemoveBgService;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ProductUploadImgImpl {


    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final RemoveBgService removeBgService;


    private final String bucketName = "productcatalogavalonplaza";

    public ProductUploadImgImpl(S3Client s3Client, S3Presigner s3Presigner, RemoveBgService removeBgService) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.removeBgService = removeBgService;
    }

    public String uploadFile(MultipartFile file, String codeBar) {

        String originalFileName = file.getOriginalFilename();


        validateImage(file);

        byte[] fileCleanBg = removeBgService.removeBackground(file);

        byte[] upscaledBytes = upscaleImage(fileCleanBg, 1080);

        // 2. Extraer la extensión (el .jpg)
        //String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + "_" + codeBar + ".png";

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType("image/png")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(upscaledBytes));

        return fileName;


    }


    public String getPresignedUrl(String fileName) {

        // VALIDACIÓN: ¿Existe realmente el archivo?
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build());

        }catch (NoSuchKeyException e) {
           // e.printStackTrace();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key("00.png")
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10)) // La URL dura 10 min
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();


        }
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // La URL dura 10 min
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private byte[] upscaleImage(byte[] imageBytes, int targetWidth) {


        // Convertir bytes a BufferedImage
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);

        try {
            BufferedImage original = ImageIO.read(bais);

            // Aplicar el Upscaling de Ultra Calidad
            // Esto compensa la pérdida de tamaño de la versión gratuita de remove.bg
            BufferedImage resized = Scalr.resize(original,
                    Scalr.Method.ULTRA_QUALITY,
                    Scalr.Mode.FIT_TO_WIDTH,
                    targetWidth);

            // Opcional: Aplicar un ligero enfoque para que no se vea borrosa tras agrandarla
            resized = Scalr.apply(resized, Scalr.OP_ANTIALIAS);

            // Convertir de nuevo a bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, "png", baos);

            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("error de escalado de imagen");
        }

    }

    public void validateImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("inserte la imagen del producto");
        }


        // 1. Lista de formatos permitidos
        List<String> allowedTypes = Arrays.asList("image/jpeg", "image/png", "image/webp");

        // 2. Verificar el tipo de contenido
        if (!allowedTypes.contains(file.getContentType())) {
            throw new RuntimeException("Formato no permitido. Solo se aceptan JPG, PNG o WEBP.");
        }

        // 3. Opcional: Validar tamaño (ejemplo: máximo 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("La imagen es demasiado pesada. Máximo 5MB.");
        }
    }

    public void deleteFile(String fileName) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            System.out.println("Imagen eliminada de S3: " + fileName);
        } catch (Exception e) {
            // Es importante capturar el error para que, si falla S3,
            // no se detenga el borrado del producto en tu DB.
            System.err.println("Error al eliminar de S3: " + e.getMessage());
        }
    }
}