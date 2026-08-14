package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of FindOutletDetailByIdUseCase Input Port.
 */
@Service
public class FindOutletDetailByIdUseCaseImpl implements FindOutletDetailByIdUseCase {

    private final OutletRepositoryPort outletRepository;
    private final ProductOutletRepositoryPort productRepository;
    private final OutletMapper outletMapper;
    private final ProductOutletMapper productMapper;

    public FindOutletDetailByIdUseCaseImpl(
            OutletRepositoryPort outletRepository,
            ProductOutletRepositoryPort productRepository,
            OutletMapper outletMapper,
            ProductOutletMapper productMapper) {
        this.outletRepository = outletRepository;
        this.productRepository = productRepository;
        this.outletMapper = outletMapper;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public OutletDetailResponse execute(Long outletId) {
        OutletDomain outletDomain = outletRepository.findById(outletId)
                .orElseThrow(() -> new ResourceNotFoundException("Outlet not found with id: " + outletId));

        List<ProductDomain> productDomains = productRepository.findAll(null, outletId, Pageable.unpaged()).getContent();

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
