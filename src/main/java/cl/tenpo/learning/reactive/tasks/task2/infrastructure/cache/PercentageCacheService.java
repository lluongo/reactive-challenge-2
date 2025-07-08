package cl.tenpo.learning.reactive.tasks.task2.infrastructure.cache;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.RedisConfig;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.TimeoutConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
@Slf4j
@RequiredArgsConstructor
public class PercentageCacheService {
    
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final TimeoutConfig timeoutConfig;

    public Mono<BigDecimal> getCachedPercentage() {
        return reactiveRedisTemplate.opsForValue().get(RedisConfig.PERCENTAGE_KEY)
                .timeout(timeoutConfig.getCacheOperation())
                .doOnNext(value -> log.debug("Valor recuperado de caché: {}", value))
                .doOnError(error -> log.error("Error accediendo a caché: {}", error.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .flatMap(this::convertCachedValueToBigDecimal)
                .checkpoint("after-cache-retrieval");
    }

    public Mono<Boolean> cachePercentage(BigDecimal percentage) {
        log.info("Almacenando porcentaje en caché: {}", percentage);
        return reactiveRedisTemplate.opsForValue()
                .set(RedisConfig.PERCENTAGE_KEY, percentage, RedisConfig.CACHE_TTL)
                .timeout(timeoutConfig.getCacheOperation())
                .doOnNext(result -> log.debug("Operación de caché completada: {}", result))
                .doOnError(error -> log.error("Error almacenando en caché: {}", error.getMessage()))
                .onErrorReturn(false);
    }

    private Mono<BigDecimal> convertCachedValueToBigDecimal(Object value) {
        return Mono.justOrEmpty(value)
                .flatMap(v -> tryConvert(v, String.class::isInstance, this::convertStringToBigDecimal)
                        .switchIfEmpty(tryConvert(v, BigDecimal.class::isInstance, v1 -> Mono.just((BigDecimal) v1)))
                        .switchIfEmpty(tryConvert(v, Number.class::isInstance, v1 -> Mono.just(new BigDecimal(((Number) v1).toString()))))
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Cannot convert value to BigDecimal: " + v))));
    }

    private <T> Mono<BigDecimal> tryConvert(Object value, Predicate<Object> predicate, Function<Object, Mono<BigDecimal>> converter) {
        return Mono.justOrEmpty(value)
                .filter(predicate)
                .flatMap(converter);
    }

    private Mono<BigDecimal> convertStringToBigDecimal(Object stringValue) {
        return Mono.fromCallable(() -> new BigDecimal((String) stringValue))
                .onErrorResume(e -> Mono.error(new IllegalArgumentException("Invalid decimal value: " + stringValue)));
    }
}
