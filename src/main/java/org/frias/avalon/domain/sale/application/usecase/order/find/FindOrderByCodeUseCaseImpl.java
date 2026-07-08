package org.frias.avalon.domain.sale.application.usecase.order.find;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.sale.application.dto.response.OrderItemResponse;
import org.frias.avalon.domain.sale.application.dto.response.OrderResponse;
import org.frias.avalon.domain.sale.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.sale.domain.OrderDomain;
import org.frias.avalon.domain.sale.domain.OrderItemDomain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindOrderByCodeUseCaseImpl implements FindOrderByCodeUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;

    @Override
    @Transactional(readOnly = true)
    public OrderResponse execute(UUID code) {
        OrderDomain order = orderRepositoryPort.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido con código '" + code + "' no encontrado."));

        MasterTree masterTree = masterTreeProvider.getTree();
        MasterRoot payNode = masterTree.getById(order.getPaymentMethodId());
        MasterRoot statusNode = masterTree.getById(order.getStatusId());

        MasterDataResponseDto payDto = payNode != null
                ? new MasterDataResponseDto(payNode.getId(), payNode.getShortName(), payNode.getFullName())
                : null;

        MasterDataResponseDto statusDto = statusNode != null
                ? new MasterDataResponseDto(statusNode.getId(), statusNode.getShortName(), statusNode.getFullName())
                : null;

        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItemDomain item : order.getItems()) {
            ProductDomain product = productOutletRepositoryPort.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + item.getProductId() + " no encontrado"));

            itemResponses.add(new OrderItemResponse(
                    product.getId(),
                    product.getName(),
                    item.getDisplayQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()
            ));
        }

        return new OrderResponse(
                order.getId(),
                order.getOrderCode(),
                order.getTotalAmount(),
                order.getOrderDate(),
                payDto,
                statusDto,
                order.getOutletId(),
                itemResponses
        );
    }
}
