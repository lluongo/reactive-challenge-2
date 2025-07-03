package cl.tenpo.learning.reactive.tasks.task2.infrastructure.cache;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.RedisConfig;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.TimeoutConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Servicio para gestionar la caché de porcentajes.
 */
@Component
@RequiredArgsConstructor
public class PercentageCacheService {

    private static final Logger log = LoggerFactory.getLogger(PercentageCacheService.class);

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final TimeoutConfig timeoutConfig;

    /**
     * Obtiene el porcentaje almacenado en caché.
     * 
     * @return Mono con el porcentaje como BigDecimal, o Mono.empty() si no existe en caché
     */
    public Mono<BigDecimal> getCachedPercentage() {
        return reactiveRedisTemplate.opsForValue().get(RedisConfig.PERCENTAGE_KEY)
                .timeout(Duration.ofSeconds(5))
                .flatMap(this::convertCachedValueToBigDecimal)
                .onErrorResume(e -> {
                    log.error("Error retrieving from cache: {}", e.getMessage());
                    return Mono.empty();
                })
                .checkpoint("after-cache-retrieval");
    }

    /**
     * Guarda un porcentaje en caché.
     * 
     * @param percentage el porcentaje a guardar
     * @return Mono con el resultado de la operación de caché
     */
    public Mono<Boolean> cachePercentage(BigDecimal percentage) {
        log.info("Caching percentage value: {}", percentage);
        return reactiveRedisTemplate.opsForValue()
                .set(RedisConfig.PERCENTAGE_KEY, percentage, RedisConfig.CACHE_TTL)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> {
                    log.error("Failed to cache percentage: {}", e.getMessage());
                    return Mono.just(false);
                })
                .doOnSuccess(result -> log.debug("Cache operation completed: {}", result));
    }

    /**
     * Convierte un valor obtenido de la caché a BigDecimal.
     * 
     * @param value valor obtenido de la caché
     * @return Mono con el porcentaje convertido a BigDecimal, o Mono.empty() si no se puede convertir
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
}
