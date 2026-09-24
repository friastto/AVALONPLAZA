package org.frias.avalon.domain.product.presentation;

import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Publicador WebSocket STOMP para cambios de catalogo, inventario y recepcion de mercancia.
 * Emite actualizaciones reactivas en tiempo real hacia las aplicaciones cliente.
 */
@Controller
public class ProductWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public ProductWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/inventory/subscribe/{outletId}")
    @SendTo("/topic/outlets/{outletId}/inventory")
    public String subscribeOutletInventory(@DestinationVariable Long outletId) {
        return "Suscrito a inventario del outlet " + outletId;
    }

    public void broadcastProductStockChanged(Long outletId, ProductResponse productResponse) {
        if (outletId != null && productResponse != null) {
            messagingTemplate.convertAndSend("/topic/outlets/" + outletId + "/inventory", productResponse);
            messagingTemplate.convertAndSend("/topic/outlets/" + outletId + "/orders", productResponse);
        }
    }
}
