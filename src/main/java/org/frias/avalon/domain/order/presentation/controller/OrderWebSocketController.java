package org.frias.avalon.domain.order.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class OrderWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/orders/subscribe/{outletId}")
    @SendTo("/topic/outlets/{outletId}/orders")
    public String subscribeOutletOrders(@DestinationVariable Long outletId) {
        return "Suscrito a actualizaciones de pedidos del outlet " + outletId;
    }

    public void broadcastOrderCreated(Long outletId, Object orderData) {
        messagingTemplate.convertAndSend("/topic/outlets/" + outletId + "/orders", orderData);
    }

    public void broadcastOrderStatusChanged(Long orderId, Object orderData) {
        messagingTemplate.convertAndSend("/topic/orders/" + orderId, orderData);
    }

    public void broadcastClaimCreated(Long orderId, Object claimData) {
        messagingTemplate.convertAndSend("/topic/orders/" + orderId + "/claims", claimData);
    }
}
