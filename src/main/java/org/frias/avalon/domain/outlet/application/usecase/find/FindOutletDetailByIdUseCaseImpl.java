package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.outlet.application.dto.response.OutletDetailResponse;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.outlet.infraestructure.mapper.OutletMapper;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;

/**
 * Implementation of FindOutletDetailByIdUseCase Input Port.
 */
@Service
public class FindOutletDetailByIdUseCaseImpl implements FindOutletDetailByIdUseCase {

    private final OutletRepositoryPort outletRepository;
    private final ProductOutletRepositoryPort productRepository;
    private final OutletMapper outletMapper;
    private final ProductOutletMapper productMapper;
    private final TransactionTemplate transactionTemplate;

    public FindOutletDetailByIdUseCaseImpl(
            OutletRepositoryPort outletRepository,
            ProductOutletRepositoryPort productRepository,
            OutletMapper outletMapper,
            ProductOutletMapper productMapper,
            PlatformTransactionManager transactionManager) {
        this.outletRepository = outletRepository;
        this.productRepository = productRepository;
        this.outletMapper = outletMapper;
        this.productMapper = productMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public OutletDetailResponse execute(Long outletId) {
        OutletDomain outletDomain = outletRepository.findById(outletId)
                .orElseThrow(() -> new ResourceNotFoundException("Outlet not found with id: " + outletId));

        if (outletDomain.getCompanyId() != null) {
            TenantContext.setTenantId(outletDomain.getCompanyId());
        }
        TenantContext.setTenantOutletId(outletDomain.getId());

        List<ProductDomain> productDomains;
        try {
            productDomains = transactionTemplate.execute(status ->
                    productRepository.findAll(null, outletId, Pageable.unpaged()).getContent()
            );
        } catch (Exception e) {
            productDomains = Collections.emptyList();
        } finally {
            TenantContext.clear();
        }

        if (productDomains == null) {
            productDomains = Collections.emptyList();
        }

        List<ProductResponse> productResponses = productDomains.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());

        OutletResponseDto baseOutletDto = outletMapper.toResponse(outletDomain);

        return new OutletDetailResponse(
                baseOutletDto.id(),
                baseOutletDto.code(),
                baseOutletDto.name(),
                baseOutletDto.address(),
                baseOutletDto.phone(),
                baseOutletDto.nit(),
                baseOutletDto.location(),
                baseOutletDto.statusResponseDto(),
                productResponses
        );
    }
}
