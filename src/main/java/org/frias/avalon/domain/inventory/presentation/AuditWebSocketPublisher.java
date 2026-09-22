package org.frias.avalon.domain.inventory.presentation;

import org.frias.avalon.domain.inventory.application.dto.AuditWebSocketMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class AuditWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public AuditWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/audit/subscribe/{outletId}/{sessionId}")
    @SendTo("/topic/audit/{outletId}/{sessionId}")
    public String subscribeAuditChannel(@DestinationVariable Long outletId, @DestinationVariable Long sessionId) {
        return "Suscrito a eventos de auditoria de tienda " + outletId + " sesion " + sessionId;
    }

    public void broadcastAuditEvent(Long outletId, Long sessionId, AuditWebSocketMessage message) {
        messagingTemplate.convertAndSend("/topic/audit/" + outletId + "/" + sessionId, message);
        messagingTemplate.convertAndSend("/topic/audit/" + outletId, message);
    }
}
