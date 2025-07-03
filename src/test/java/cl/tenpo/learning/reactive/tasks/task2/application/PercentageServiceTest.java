package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.cache.PercentageCacheService;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.client.ExternalApiClient;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.retry.RetryStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;
import java.math.BigDecimal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PercentageServiceTest {

    @Mock
    private ExternalApiClient externalApiClient;
    @Mock
    private PercentageCacheService cacheService;
    @Mock
    private RetryStrategy retryStrategy;
    private ExternalPercentageService service;

    @BeforeEach
    void setUp() {
        service = new ExternalPercentageService(externalApiClient, cacheService, retryStrategy);
        when(retryStrategy.getRetrySpec(any())).thenReturn(Retry.max(1));
    }

    @Test
    void testGetPercentageFromExternalApi() {
        BigDecimal expectedValue = BigDecimal.valueOf(0.25);
        when(cacheService.getCachedPercentage()).thenReturn(Mono.empty());
        when(externalApiClient.fetchPercentage()).thenReturn(Mono.just(expectedValue));
        when(cacheService.cachePercentage(expectedValue)).thenReturn(Mono.just(true));
        StepVerifier.create(service.getPercentage())
                .expectNext(expectedValue)
                .verifyComplete();
    }

    @Test
    void testServiceCanReturnValue() {
        BigDecimal value = BigDecimal.valueOf(0.15);
        when(cacheService.getCachedPercentage()).thenReturn(Mono.just(value).then(Mono.empty()));
        when(externalApiClient.fetchPercentage()).thenReturn(Mono.just(value));
        when(cacheService.cachePercentage(any())).thenReturn(Mono.just(true));
        StepVerifier.create(service.getPercentage())
                .expectNext(value)
                .verifyComplete();
    }

    @Test
    void testServiceHandlesApiCall() {
        BigDecimal value = BigDecimal.valueOf(0.30);
        when(cacheService.getCachedPercentage()).thenReturn(Mono.empty());
        when(externalApiClient.fetchPercentage()).thenReturn(Mono.just(value));
        when(cacheService.cachePercentage(value)).thenReturn(Mono.just(true));
        StepVerifier.create(service.getPercentage())
                .expectNext(value)
                .verifyComplete();
    }
}
