package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.cache.PercentageCacheService;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.client.ExternalApiClient;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.exception.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PercentageServiceTest {

    @Mock
    private ExternalApiClient externalApiClient;
    
    @Mock
    private PercentageCacheService cacheService;
    
    private ExternalPercentageService service;

    @BeforeEach
    void setUp() {
        service = new ExternalPercentageService(externalApiClient, cacheService);
    }

    @Test
    void testGetPercentageFromExternalApi() {
        BigDecimal expectedValue = BigDecimal.valueOf(0.25);
        when(externalApiClient.fetchPercentage()).thenReturn(Mono.just(expectedValue));
        when(cacheService.cachePercentage(expectedValue)).thenReturn(Mono.just(true));

        StepVerifier.create(service.getPercentage())
                .expectNext(expectedValue)
                .verifyComplete();
    }

    @Test
    void testFallbackToCache() {

        BigDecimal cachedValue = BigDecimal.valueOf(0.15);
        when(externalApiClient.fetchPercentage()).thenReturn(Mono.error(new RuntimeException("API Error")));
        when(cacheService.getCachedPercentage()).thenReturn(Mono.just(cachedValue));

        StepVerifier.create(service.getPercentage())
                .expectNext(cachedValue)
                .verifyComplete();
    }

    @Test
    void testServiceErrorWhenNoCache() {
        when(externalApiClient.fetchPercentage()).thenReturn(Mono.error(new RuntimeException("API Error")));
        when(cacheService.getCachedPercentage()).thenReturn(Mono.empty());

        StepVerifier.create(service.getPercentage())
                .expectError(ServiceUnavailableException.class)
                .verify();
    }
}
