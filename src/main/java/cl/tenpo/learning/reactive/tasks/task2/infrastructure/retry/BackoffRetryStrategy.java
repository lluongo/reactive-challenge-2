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

@Slf4j
@Component
@RequiredArgsConstructor

public class BackoffRetryStrategy implements RetryStrategy {

    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.retry.max-attempts}")
    private long maxAttempts;

    @Value("${app.retry.initial-backoff}")
    private Duration initialBackoff;

    @Value("${app.retry.max-backoff}")
    private Duration maxBackoff;

    @Value("${app.retry.backoff-multiplier}")
    private double backoffMultiplier;

    @Override
    public <T> Retry getRetrySpec(Class<T> targetClass) {
        log.error("🚀🚀🚀 CREATING RETRY SPEC FOR CLASS: {} WITH MAX ATTEMPTS: {} 🚀🚀🚀", targetClass.getSimpleName(), maxAttempts);

        return Retry.backoff(maxAttempts, initialBackoff)
                .doBeforeRetry(signal -> log.error("⏳⏳⏳ ABOUT TO RETRY #{}/{} FOR {}: {} ⏳⏳⏳", 
                        signal.totalRetries() + 1, maxAttempts, targetClass.getSimpleName(), signal.failure().getMessage()))
                .doAfterRetry(signal -> log.error("🔄🔄🔄 COMPLETED RETRY #{}/{} FOR {} 🔄🔄🔄",
                        signal.totalRetries(), maxAttempts, targetClass.getSimpleName()))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    log.error("💣💣💣 RETRY EXHAUSTED FOR {}: {} 💣💣💣", 
                            targetClass.getSimpleName(), retrySignal.failure().getMessage());

                    // Publicar evento para Kafka
                    Map<String, String> errorData = new HashMap<>();
                    errorData.put("error", retrySignal.failure().getMessage());
                    errorData.put("targetClass", targetClass.getName());
                    errorData.put("attempts", String.valueOf(maxAttempts));

                    eventPublisher.publishEvent(new RetryExhaustedEvent(errorData));

                    return new ServiceUnavailableException("Failed to fetch BigDecimal after " + maxAttempts + " attempts");
                });
    }
}
