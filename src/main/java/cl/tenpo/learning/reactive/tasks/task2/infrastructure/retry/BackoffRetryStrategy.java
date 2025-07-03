package cl.tenpo.learning.reactive.tasks.task2.infrastructure.retry;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.event.RetryExhaustedEvent;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementación de estrategia de reintentos con backoff.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BackoffRetryStrategy implements RetryStrategy {

    private final ApplicationEventPublisher eventPublisher;
    
    @Value("${app.retry.max-attempts:3}")
    private int maxAttempts;
    
    @Value("${app.retry.initial-backoff:PT1S}")
    private Duration initialBackoff;

    @Override
    public <T> Retry getRetrySpec(Class<T> targetClass) {
        return Retry.backoff(maxAttempts, initialBackoff)
                .doAfterRetry(signal -> log.warn("Retry attempt {}: {}", 
                        signal.totalRetries() + 1, signal.failure().getMessage()))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    String errorMsg = "Failed to fetch " + targetClass.getSimpleName() + " after " + maxAttempts + " attempts";
                    log.error(errorMsg);
                    
                    Map<String, String> eventData = new HashMap<>();
                    eventData.put("error", errorMsg + ": " + retrySignal.failure().getMessage());
                    eventPublisher.publishEvent(new RetryExhaustedEvent(eventData));
                    
                    return new ServiceUnavailableException(errorMsg);
                });
    }
}
