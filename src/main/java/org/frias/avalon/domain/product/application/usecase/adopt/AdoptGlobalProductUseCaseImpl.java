package org.frias.avalon.domain.product.application.usecase.adopt;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.application.dto.request.AdoptGlobalProductRequestDto;
import org.frias.avalon.domain.product.application.dto.response.ProductSuggestionResponseDto;
import org.frias.avalon.domain.product.application.usecase.propagate.PropagateCompanyProductToOutletsUseCase;
import org.frias.avalon.domain.product.infraestructure.entity.Product;
import org.frias.avalon.domain.product.infraestructure.repository.JpaGlobalProductRepository;
import org.frias.avalon.domain.product.infrastructure.entity.ProductCompanyEntity;
import org.frias.avalon.domain.product.infrastructure.repository.JpaProductCompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Caso de uso para adoptar un producto del Catalogo Global (Nivel 1) al Catalogo de Empresa (Nivel 2)
 * y propagarlo automaticamente a todas las tiendas de la compania (Nivel 3).
 */
@Service
public class AdoptGlobalProductUseCaseImpl implements AdoptGlobalProductUseCase {

    private final JpaGlobalProductRepository globalProductRepository;
    private final JpaProductCompanyRepository productCompanyRepository;
    private final PropagateCompanyProductToOutletsUseCase propagateCompanyProductToOutletsUseCase;
    private final CompanyRepositoryPort companyRepositoryPort;
    private final CurrentUserProviderPort currentUserProvider;
    private final MasterTreeProvider masterTreeProvider;

    public AdoptGlobalProductUseCaseImpl(
            JpaGlobalProductRepository globalProductRepository,
            JpaProductCompanyRepository productCompanyRepository,
            PropagateCompanyProductToOutletsUseCase propagateCompanyProductToOutletsUseCase,
            CompanyRepositoryPort companyRepositoryPort,
            CurrentUserProviderPort currentUserProvider,
            MasterTreeProvider masterTreeProvider
    ) {
        this.globalProductRepository = globalProductRepository;
        this.productCompanyRepository = productCompanyRepository;
        this.propagateCompanyProductToOutletsUseCase = propagateCompanyProductToOutletsUseCase;
        this.companyRepositoryPort = companyRepositoryPort;
        this.currentUserProvider = currentUserProvider;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    @Transactional
    public ProductSuggestionResponseDto execute(AdoptGlobalProductRequestDto request) {
        Product product = globalProductRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto global con ID " + request.productId() + " no encontrado"));

        Long targetCompanyId = request.companyId();
        if (targetCompanyId == null) {
            targetCompanyId = currentUserProvider.getCurrentTenantId();
        }
        if (targetCompanyId == null) {
            throw new BusinessException("No se especifico la compania para realizar la adopcion del producto.");
        }

        // Validacion de seguridad RBAC para GERGEN
        if (currentUserProvider.hasRole("ROLE_GERGEN")) {
            Long userCompanyId = currentUserProvider.getCurrentTenantId();
            if (userCompanyId != null && !userCompanyId.equals(targetCompanyId)) {
                throw new BusinessException("Acceso denegado: No tiene permisos para adoptar productos para otra compania.");
            }
        }

        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot actStatus = tree.getByCode("ACT");
        Long activeStatusId = actStatus != null ? actStatus.getId() : 1L;

        Optional<ProductCompanyEntity> existingOpt = productCompanyRepository
                .findByProductIdAndCompanyId(product.getId(), targetCompanyId);

        ProductCompanyEntity productCompany;
        if (existingOpt.isPresent()) {
            productCompany = existingOpt.get();
            productCompany.setCustomPrice(request.customPrice());
            if (request.customImageUrl() != null && !request.customImageUrl().isBlank()) {
                productCompany.setCustomImageUrl(request.customImageUrl());
            }
            productCompany.setStatusId(activeStatusId);
        } else {
            productCompany = ProductCompanyEntity.builder()
                    .productId(product.getId())
                    .companyId(targetCompanyId)
                    .customPrice(request.customPrice())
                    .customImageUrl(request.customImageUrl())
                    .statusId(activeStatusId)
                    .build();
        }

        ProductCompanyEntity savedProductCompany = productCompanyRepository.save(productCompany);

        // Propagacion en cascada inmediata a todas las tiendas de la empresa (Nivel 3)
        propagateCompanyProductToOutletsUseCase.execute(savedProductCompany.getId());

        String companyName = companyRepositoryPort.findById(targetCompanyId)
                .map(CompanyDomain::name)
                .orElse("Compania " + targetCompanyId);

        return new ProductSuggestionResponseDto(
                savedProductCompany.getId(),
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBarcode(),
                MasterRefDto.from(product.getCategoryId() != null ? tree.getById(product.getCategoryId()) : null),
                MasterRefDto.from(product.getUnitMeasureId() != null ? tree.getById(product.getUnitMeasureId()) : null),
                savedProductCompany.getCustomPrice(),
                savedProductCompany.getCustomImageUrl() != null ? savedProductCompany.getCustomImageUrl() : product.getImageUrl(),
                targetCompanyId,
                companyName,
                null,
                null,
                MasterRefDto.from(savedProductCompany.getStatusId() != null ? tree.getById(savedProductCompany.getStatusId()) : null),
                savedProductCompany.getCreatedAt()
        );
    }
}
