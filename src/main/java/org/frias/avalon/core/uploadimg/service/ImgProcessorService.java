package org.frias.avalon.core.uploadimg.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

public interface ImgProcessorService {

    void processProductImage(Long productId, byte[] imgUrl, String barcode, String targetType);


}
