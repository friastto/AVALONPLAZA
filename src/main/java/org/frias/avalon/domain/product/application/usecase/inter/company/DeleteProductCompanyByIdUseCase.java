package org.frias.avalon.domain.product.application.usecase.inter.company;

import org.springframework.stereotype.Service;



public interface DeleteProductCompanyByIdUseCase {
    void execute(Long id);
}