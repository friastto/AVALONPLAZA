package org.frias.avalon.core.uploadimg.service;

public interface ImgProcessorService {

    void processProductImage(Long productId, byte[] imgUrl, String barcode, String targetType);


}
