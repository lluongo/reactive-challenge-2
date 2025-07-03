package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.application.port.PercentageService;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.cache.PercentageCacheService;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.client.ExternalApiClient;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.retry.RetryStrategy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor

public class ExternalPercentageService implements PercentageService {

    private final ExternalApiClient externalApiClient;
    private final PercentageCacheService cacheService;
    private final RetryStrategy retryStrategy;

    @PostConstruct
    public void init() {
        log.error("🚀🚀🚀 PERCENTAGE SERVICE INITIALIZED 🚀🚀🚀");
    }

    @Override
    public Mono<BigDecimal> getPercentage() {
        log.error("🎯🎯🎯 GETPERCENTAGE() CALLED - CHECKING CACHE FIRST 🎯🎯🎯");
        return cacheService.getCachedPercentage()
                .doOnNext(cached -> log.error("✅✅✅ CACHE HIT - USING CACHED VALUE: {} ✅✅✅", cached))
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("❌❌❌ CACHE MISS - CALLING fetchAndCachePercentage() ❌❌❌");
                    return fetchAndCachePercentage().checkpoint("after-fetch-and-cache");
                }))
                .doOnSubscribe(s -> log.error("🔔🔔🔔 PERCENTAGE SERVICE SUBSCRIBED 🔔🔔🔔"))
                .doOnSuccess(val -> log.error("🎉🎉🎉 PERCENTAGE SERVICE SUCCESS: {} 🎉🎉🎉", val))
                .doOnError(err -> log.error("💥💥💥 PERCENTAGE SERVICE ERROR: {} 💥💥💥", err.getMessage()));
    }

    private Mono<BigDecimal> fetchAndCachePercentage() {
        log.error("🔄🔄🔄 FETCH AND CACHE PERCENTAGE - CALLING EXTERNAL API WITH RETRY 🔄🔄🔄");
        return externalApiClient.fetchPercentage()
                .retryWhen(retryStrategy.getRetrySpec(BigDecimal.class))
                .flatMap(percentage -> cacheService.cachePercentage(percentage)
                        .thenReturn(percentage))
                .doOnNext(percentage -> log.error("💾💾💾 CACHED PERCENTAGE: {} 💾💾💾", percentage));
    }
}
