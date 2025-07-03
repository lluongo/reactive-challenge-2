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

@Component
@RequiredArgsConstructor
public class PercentageCacheService {

    private static final Logger log = LoggerFactory.getLogger(PercentageCacheService.class);
    
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final TimeoutConfig timeoutConfig;

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

    private Mono<BigDecimal> convertCachedValueToBigDecimal(Object value) {
        return Mono.justOrEmpty(value)
                .cast(Object.class)
                .flatMap(this::convertByType)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Unexpected type in cache or null value: {}", 
                        value != null ? value.getClass().getName() : "null");
                    return Mono.empty();
                }));
    }

    private Mono<BigDecimal> convertByType(Object value) {
        return Mono.just(value)
                .filter(BigDecimal.class::isInstance)
                .cast(BigDecimal.class)
                .doOnNext(v -> log.info("Retrieved percentage from cache: {}", v))
                .switchIfEmpty(
                    Mono.just(value)
                        .filter(Double.class::isInstance)
                        .cast(Double.class)
                        .map(BigDecimal::valueOf)
                        .doOnNext(v -> log.info("Retrieved percentage from cache (as Double): {}", v))
                )
                .switchIfEmpty(
                    Mono.just(value)
                        .filter(Number.class::isInstance)
                        .cast(Number.class)
                        .map(n -> BigDecimal.valueOf(n.doubleValue()))
                        .doOnNext(v -> log.info("Retrieved percentage from cache (as Number): {}", v))
                )
                .switchIfEmpty(
                    Mono.just(value)
                        .filter(String.class::isInstance)
                        .cast(String.class)
                        .flatMap(this::convertStringToBigDecimal)
                );
    }

    private Mono<BigDecimal> convertStringToBigDecimal(String stringValue) {
        return Mono.fromCallable(() -> new BigDecimal(stringValue))
                .doOnNext(v -> log.info("Retrieved percentage from cache (as String): {}", v))
                .onErrorResume(NumberFormatException.class, error -> {
                    log.warn("Invalid cached percentage format: {}", stringValue);
                    return Mono.empty();
                });
    }
}
