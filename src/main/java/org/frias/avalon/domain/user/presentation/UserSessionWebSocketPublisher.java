package org.frias.avalon.domain.user.presentation;

import org.frias.avalon.domain.user.application.dtos.response.SessionSyncMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * WebSocket publisher for user session lifecycle events.
 * Broadcasts status updates directly to the affected user's topic.
 */
@Component
public class UserSessionWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public UserSessionWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcasts a session synchronization event to the user's private topic channel.
     *
     * @param userId  User identifier receiving the event.
     * @param message Payload containing event details (e.g. status code, outlet).
     */
    public void broadcastSessionSync(Long userId, SessionSyncMessage message) {
        if (userId != null && message != null) {
            messagingTemplate.convertAndSend("/topic/users/" + userId + "/session-sync", message);
        }
    }
}
