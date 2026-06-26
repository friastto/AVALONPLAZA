package org.frias.avalon.domain.outlet.application.usecase.find;

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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FindOutletDetailByIdUseCase {

    private final OutletRepositoryPort outletRepository;
    private final ProductOutletRepositoryPort productRepository;
    private final OutletMapper outletMapper;
    private final ProductOutletMapper productMapper;

    public FindOutletDetailByIdUseCase(
            OutletRepositoryPort outletRepository,
            ProductOutletRepositoryPort productRepository,
            OutletMapper outletMapper,
            ProductOutletMapper productMapper) {
        this.outletRepository = outletRepository;
        this.productRepository = productRepository;
        this.outletMapper = outletMapper;
        this.productMapper = productMapper;
    }

    public OutletDetailResponse execute(Long outletId) {
        // 1. Retrieve the main aggregate from its repository
        OutletDomain outletDomain = outletRepository.findById(outletId)
                .orElseThrow(() -> new RuntimeException("Outlet not found with id: " + outletId));

        // 2. Retrieve related data from other repositories
        List<ProductDomain> productDomains = productRepository.findAll(null, outletId, Pageable.unpaged()).getContent();

        // 3. Use the dedicated Product mapper to convert domain objects to response DTOs
        List<ProductResponse> productResponses = productDomains.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());

        // 4. Use the infrastructure OutletMapper to get the base response DTO
        OutletResponseDto baseOutletDto = outletMapper.toResponse(outletDomain);

        // 5. Assemble the final detail DTO
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