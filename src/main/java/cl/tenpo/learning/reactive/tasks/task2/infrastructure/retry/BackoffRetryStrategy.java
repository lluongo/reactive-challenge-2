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

    @Override
    public <T> Retry getRetrySpec(Class<T> targetClass) {
        String className = targetClass.getSimpleName();
        log.info("Creating retry spec for {} (maxAttempts={}, initialBackoff={})", 
            className, maxAttempts, initialBackoff);

        return Retry.backoff(maxAttempts, initialBackoff)
                .maxBackoff(maxBackoff)
                .doBeforeRetry(signal -> 
                    log.warn("Retry attempt #{}/{} for {} - Cause: {}", 
                        signal.totalRetries() + 1, maxAttempts, className, signal.failure().getMessage()))
                .doAfterRetry(signal -> 
                    log.info("Completed retry #{}/{} for {}", 
                        signal.totalRetries(), maxAttempts, className))
                .onRetryExhaustedThrow((spec, signal) -> {
                    publishRetryExhaustedEvent(className, signal.failure());
                    return new ServiceUnavailableException(
                            "Retry exhausted for " + className + " after " + maxAttempts + " attempts", 
                            signal.failure());
                });
    }
    
    private void publishRetryExhaustedEvent(String className, Throwable error) {
        log.error("Retry strategy exhausted for {} - publishing event", className);

        Map<String, String> errorData = Map.of(
            "class", className,
            "error", error.getMessage(),
            "attempts", String.valueOf(maxAttempts)
        );

        eventPublisher.publishEvent(new RetryExhaustedEvent(errorData));
    }
}
