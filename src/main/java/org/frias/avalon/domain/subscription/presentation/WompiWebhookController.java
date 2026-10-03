package org.frias.avalon.domain.subscription.presentation;

import org.frias.avalon.domain.subscription.application.usecase.ProcessWompiWebhookUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public webhook endpoint for receiving transactional events from Wompi Colombia.
 */
@RestController
@RequestMapping({"/avalon/webhooks/wompi", "/api/v1/webhooks/wompi"})
public class WompiWebhookController {

    private final ProcessWompiWebhookUseCase processWompiWebhookUseCase;

    public WompiWebhookController(ProcessWompiWebhookUseCase processWompiWebhookUseCase) {
        this.processWompiWebhookUseCase = processWompiWebhookUseCase;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> receiveWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "x-event-checksum", required = false) String checksumHeader
    ) {
        processWompiWebhookUseCase.execute(payload, payload != null ? payload.toString() : "");
        return ResponseEntity.ok(Map.of("status", "received"));
    }
}
