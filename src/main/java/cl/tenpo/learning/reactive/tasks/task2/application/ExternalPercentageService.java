package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.RedisConfig;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.TimeoutConfig;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.event.RetryExhaustedEvent;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalPercentageService {

    private final WebClient webClient;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final TimeoutConfig timeoutConfig;

    /**
     * Obtiene el porcentaje a aplicar en los cálculos.
     * Primero intenta obtenerlo desde la caché, si no está presente o hay error,
     * lo obtiene del servicio externo.
     * 
     * @return Mono con el porcentaje como BigDecimal
     */
    public Mono<BigDecimal> getPercentage() {
        // First try to get percentage from cache
        return reactiveRedisTemplate.opsForValue().get(RedisConfig.PERCENTAGE_KEY)
                .timeout(timeoutConfig.getCacheTimeout())
                .flatMap(value -> convertCachedValueToBigDecimal(value))
                .onErrorResume(e -> {
                    log.error("Error retrieving from cache: {}", e.getMessage());
                    return Mono.empty();
                })
                .checkpoint("after-cache-retrieval")
                .switchIfEmpty(fetchAndCachePercentage().checkpoint("after-fetch-and-cache"));
    }

    /**
     * Convierte un valor obtenido de la caché a BigDecimal, manejando diferentes tipos posibles.
     */
    private Mono<BigDecimal> convertCachedValueToBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            log.info("Retrieved percentage from cache: {}", value);
            return Mono.just((BigDecimal) value);
        } else if (value instanceof Double) {
            log.info("Retrieved percentage from cache (as Double): {}", value);
            return Mono.just(BigDecimal.valueOf((Double) value));
        } else if (value instanceof Number) {
            log.info("Retrieved percentage from cache (as Number): {}", value);
            return Mono.just(BigDecimal.valueOf(((Number) value).doubleValue()));
        } else if (value instanceof String) {
            try {
                log.info("Retrieved percentage from cache (as String): {}", value);
                return Mono.just(new BigDecimal((String) value));
            } catch (NumberFormatException e) {
                log.warn("Invalid cached percentage format: {}", value);
                return Mono.empty();
            }
        }
        log.warn("Unexpected type in cache: {}", value.getClass().getName());
        return Mono.empty();
    }

    /**
     * Obtiene el porcentaje del API externo y lo guarda en caché.
     */
    private Mono<BigDecimal> fetchAndCachePercentage() {
        log.info("Fetching percentage from external API");
        return webClient.get()
                .uri("/external-api/percentage")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(timeoutConfig.getExternalApiTimeout())
                .map(this::extractPercentageFromResponse)
                .flatMap(percentage -> cachePercentage(percentage).thenReturn(percentage))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .doAfterRetry(signal -> log.warn("Retry attempt {}: {}", 
                                signal.totalRetries() + 1, signal.failure().getMessage()))
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            String errorMsg = "Failed to fetch percentage after 3 attempts";
                            log.error(errorMsg);
                            
                            // Publicar el evento con el formato requerido
                            Map<String, String> eventData = new HashMap<>();
                            eventData.put("error", errorMsg + ": " + retrySignal.failure().getMessage());
                            eventPublisher.publishEvent(new RetryExhaustedEvent(eventData));
                            
                            // Devolver un error HTTP adecuado (503 Service Unavailable)
                            return new ServiceUnavailableException(errorMsg);
                        }))
                .log("external-percentage-service");
    }
    
    /**
     * Extrae el porcentaje de la respuesta del API.
     */
    private BigDecimal extractPercentageFromResponse(Map<String, Object> response) {
        String percentageStr = (String) response.get("percentage");
        log.info("Received percentage from external API: {}", percentageStr);
        return new BigDecimal(percentageStr);
    }
    
    /**
     * Guarda el porcentaje en caché de manera reactiva.
     */
    private Mono<Boolean> cachePercentage(BigDecimal percentage) {
        log.info("Caching percentage value: {}", percentage);
        return reactiveRedisTemplate.opsForValue()
                .set(RedisConfig.PERCENTAGE_KEY, percentage, RedisConfig.CACHE_TTL)
                .timeout(timeoutConfig.getCacheTimeout())
                .onErrorResume(e -> {
                    log.error("Failed to cache percentage: {}", e.getMessage());
                    return Mono.just(false);
                })
                .doOnSuccess(result -> log.debug("Cache operation completed: {}", result));
    }
}
