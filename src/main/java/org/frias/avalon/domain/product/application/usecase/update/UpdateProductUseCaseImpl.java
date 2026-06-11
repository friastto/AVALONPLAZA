package org.frias.avalon.domain.product.application.usecase.update;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.application.dto.request.ProductUpdateRequest;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.application.service.QuantityParserService;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UpdateProductUseCaseImpl implements UpdateProductUseCase {

    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final QuantityParserService quantityParserService;
    private final UnitConversionService unitConversionService;
    private final ProductOutletMapper productOutletMapper;

    @Override
    @Transactional
    public ProductResponse execute(Long productId, ProductUpdateRequest request) {
        // 1. Buscar el producto existente
        ProductDomain productDomain = productOutletRepositoryPort.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("El producto con ID " + productId + " no existe."));

        // 2. Parsear y validar la cantidad usando el Application Service
        BigDecimal validQuantity = quantityParserService.parseAndValidate(request.stockQuantity());

        // 3. Validar la unidad de medida contra el MasterData
        MasterTree masterTree = masterTreeProvider.getTree();
        MasterRoot unitNode = masterTree.getById(request.stockUnitId());
        
        if (unitNode == null) {
            throw new DomainValidationException("El ID de la unidad de medida proporcionado no existe.");
        }
        if (!masterTree.isChildOf(unitNode, "UNIT")) {
            throw new DomainValidationException("El ID proporcionado no es una unidad de medida válida.");
        }

        // 4. Usar el Domain Service para convertir la cantidad a la unidad base
        String unitCode = unitNode.getShortName();
        Integer stockInBaseUnits = unitConversionService.convertToSmallestUnit(validQuantity, unitCode);

        // 5. Aplicar los cambios al modelo de dominio (Rich Domain Model)
        productDomain.updateDetails(
                request.name(),
                request.description(),
                stockInBaseUnits,
                request.stockUnitId(),
                request.imageUrl(),
                request.price()
        );

        // 6. Guardar los cambios
        ProductDomain updatedProduct = productOutletRepositoryPort.save(productDomain);

        // 7. Mapear y devolver
        return productOutletMapper.toResponse(updatedProduct);
    }
}
