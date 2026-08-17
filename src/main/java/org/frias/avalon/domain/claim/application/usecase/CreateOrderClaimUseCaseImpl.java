package org.frias.avalon.domain.claim.application.usecase;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.claim.application.dto.request.CreateOrderClaimRequest;
import org.frias.avalon.domain.claim.application.dto.response.ClaimResponse;
import org.frias.avalon.domain.claim.application.port.ClaimRepositoryPort;
import org.frias.avalon.domain.claim.domain.OrderClaimDomain;
import org.frias.avalon.domain.claim.domain.OrderClaimItemDomain;
import org.frias.avalon.domain.claim.domain.OrderClaimPhotoDomain;
import org.frias.avalon.domain.claim.infrastructure.persistence.mapper.ClaimMapper;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.presentation.controller.OrderWebSocketController;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateOrderClaimUseCaseImpl implements CreateOrderClaimUseCase {

    private final ClaimRepositoryPort claimRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final ClaimMapper claimMapper;
    private final OrderWebSocketController orderWebSocketController;

    @Override
    @Transactional
    public ClaimResponse execute(CreateOrderClaimRequest request) {
        orderRepositoryPort.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido con ID " + request.getOrderId() + " no encontrado"));

        Long clmPenStatusId = masterDataRepositoryPort.getIdByCode("CLM_PEN");
        if (clmPenStatusId == null) {
            clmPenStatusId = masterDataRepositoryPort.getIdByCode("PEN");
        }
        if (clmPenStatusId == null) {
            clmPenStatusId = 1L;
        }

        LocalDateTime now = LocalDateTime.now();

        List<OrderClaimItemDomain> claimItems = new ArrayList<>();
        for (var itemReq : request.getItems()) {
            claimItems.add(OrderClaimItemDomain.builder()
                    .orderItemId(itemReq.getOrderItemId())
                    .quantityAffected(itemReq.getQuantityAffected())
                    .reason(itemReq.getReason())
                    .build());
        }

        List<OrderClaimPhotoDomain> claimPhotos = new ArrayList<>();
        if (request.getPhotoUrls() != null) {
            for (String photoUrl : request.getPhotoUrls()) {
                claimPhotos.add(OrderClaimPhotoDomain.builder()
                        .photoUrl(photoUrl)
                        .createdAt(now)
                        .build());
            }
        }

        OrderClaimDomain domain = OrderClaimDomain.builder()
                .orderId(request.getOrderId())
                .customerId(request.getCustomerId())
                .claimTypeId(request.getClaimTypeId())
                .statusId(clmPenStatusId)
                .description(request.getDescription())
                .createdAt(now)
                .updatedAt(now)
                .items(claimItems)
                .photos(claimPhotos)
                .build();

        OrderClaimDomain saved = claimRepositoryPort.save(domain);
        ClaimResponse response = claimMapper.toResponse(saved);

        orderWebSocketController.broadcastClaimCreated(request.getOrderId(), response);

        return response;
    }
}
