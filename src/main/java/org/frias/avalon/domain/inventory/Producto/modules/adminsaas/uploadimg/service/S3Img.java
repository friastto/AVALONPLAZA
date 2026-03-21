package org.frias.avalon.domain.inventory.Producto.modules.adminsaas.uploadimg.service;


import lombok.Getter;
import lombok.Setter;


public record  S3Img(
        String fileNameBucket,
         String urlS3bucket) {


}
