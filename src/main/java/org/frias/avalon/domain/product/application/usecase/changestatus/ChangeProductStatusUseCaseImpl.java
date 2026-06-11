package org.frias.avalon.domain.product.application.usecase.changestatus;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.application.dto.request.ChangeStatusRequest;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeProductStatusUseCaseImpl implements ChangeProductStatusUseCase {

    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final ProductOutletMapper productOutletMapper;

    @Override
    @Transactional
    public ProductResponse execute(Long productId, ChangeStatusRequest request) {
        // 1. Buscar el producto existente
        ProductDomain productDomain = productOutletRepositoryPort.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("El producto con ID " + productId + " no existe."));

        // 2. Validar que el nuevo ID de estado es un estado de producto válido
        MasterTree masterTree = masterTreeProvider.getTree();
        MasterRoot statusNode = masterTree.getById(request.newStatusId());

        if (statusNode == null) {
            throw new DomainValidationException("El ID de estado proporcionado no existe.");
        }
        // Asumimos que los estados de producto cuelgan de un nodo padre con código "PRODUCT_STATUS"
        if (!masterTree.isChildOf(statusNode, "STSGEN")) {
            throw new DomainValidationException("El ID proporcionado no corresponde a un estado de producto válido.");
        }

        // 3. Delegar el cambio de estado al modelo de dominio
        productDomain.changeStatus(request.newStatusId());

        // 4. Guardar los cambios
        ProductDomain updatedProduct = productOutletRepositoryPort.save(productDomain);

        // 5. Mapear y devolver el resultado
        return productOutletMapper.toResponse(updatedProduct);
    }
}
