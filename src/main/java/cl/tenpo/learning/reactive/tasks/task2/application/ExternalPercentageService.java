package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.application.port.PercentageService;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.cache.PercentageCacheService;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.client.ExternalApiClient;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.retry.RetryStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Implementación del servicio de porcentajes que combina caché y llamadas a API externo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalPercentageService implements PercentageService {

    private final ExternalApiClient externalApiClient;
    private final PercentageCacheService cacheService;
    private final RetryStrategy retryStrategy;

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<BigDecimal> getPercentage() {
        return cacheService.getCachedPercentage()
                .switchIfEmpty(fetchAndCachePercentage().checkpoint("after-fetch-and-cache"));
    }

    /**
     * Obtiene el porcentaje del API externo y lo guarda en caché.
     */
    private Mono<BigDecimal> fetchAndCachePercentage() {
        return externalApiClient.fetchPercentage()
                .flatMap(percentage -> cacheService.cachePercentage(percentage).thenReturn(percentage))
                .retryWhen(retryStrategy.getRetrySpec(BigDecimal.class))
                .log("external-percentage-service");
    }
}
