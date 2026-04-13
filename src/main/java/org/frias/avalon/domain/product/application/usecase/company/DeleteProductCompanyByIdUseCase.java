package org.frias.avalon.domain.product.application.usecase.company;

import org.springframework.stereotype.Service;



public interface DeleteProductCompanyByIdUseCase {
    void execute(Long id);
}