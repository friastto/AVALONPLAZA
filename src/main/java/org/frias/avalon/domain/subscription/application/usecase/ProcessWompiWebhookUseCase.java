package org.frias.avalon.domain.subscription.application.usecase;

import java.util.Map;

/**
 * Use case to process Wompi payment webhook events idempotently.
 */
public interface ProcessWompiWebhookUseCase {
    void execute(Map<String, Object> payload, String rawBody);
}
