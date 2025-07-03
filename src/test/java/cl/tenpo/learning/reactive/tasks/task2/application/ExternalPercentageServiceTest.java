package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.cache.PercentageCacheService;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.client.ExternalApiClient;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.exception.ServiceUnavailableException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExternalPercentageServiceTest {

    @Mock
    private ExternalApiClient externalApiClient;

    @Mock
    private PercentageCacheService cacheService;

    @Mock
    private RetryStrategy retryStrategy;

    private ExternalPercentageService percentageService;

    @BeforeEach
    void setUp() {
        percentageService = new ExternalPercentageService(externalApiClient, cacheService, retryStrategy);
        
        // Mock para RetryStrategy
        when(retryStrategy.getRetrySpec(any())).thenReturn(Retry.max(1));
    }

    @Test
    void getPercentage_whenCacheHasValue_shouldReturnCachedValue() {
        // Given - Un valor en la caché
        BigDecimal expectedValue = new BigDecimal("0.1");
        when(cacheService.getCachedPercentage()).thenReturn(Mono.just(expectedValue));

        // When - Obtener el porcentaje
        Mono<BigDecimal> result = percentageService.getPercentage();
        
        // Then - Verificar que se usa el valor cacheado
        StepVerifier.create(result)
                .expectNext(expectedValue)
                .verifyComplete();
                
        // Verificar que se intentó obtener de la caché
        verify(cacheService).getCachedPercentage();
        
        // Verificar que no se intentó obtener del API externo
        verify(externalApiClient, never()).fetchPercentage();
    }

    @Test
    void getPercentage_whenCacheEmptyAndApiSucceeds_shouldFetchFromApiAndCache() {
        // Given
        BigDecimal expectedValue = new BigDecimal("0.15");
        
        when(cacheService.getCachedPercentage()).thenReturn(Mono.empty());
        when(externalApiClient.fetchPercentage()).thenReturn(Mono.just(expectedValue));
        when(cacheService.cachePercentage(expectedValue)).thenReturn(Mono.just(Boolean.TRUE));

        // When & Then
        StepVerifier.create(percentageService.getPercentage())
                .expectNext(expectedValue)
                .verifyComplete();
    }

    @Test
    void getPercentage_whenCacheEmptyAndApiFails_shouldThrowServiceUnavailableException() {
        // Given
        when(cacheService.getCachedPercentage()).thenReturn(Mono.empty());
        when(externalApiClient.fetchPercentage()).thenReturn(Mono.error(new RuntimeException("API Error")));
        when(retryStrategy.getRetrySpec(any())).thenReturn(Retry.max(1)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> 
                    new ServiceUnavailableException("Failed to fetch percentage")));
        
        // When & Then
        StepVerifier.create(percentageService.getPercentage())
                .expectError(ServiceUnavailableException.class)
                .verify();
    }

    @Test
    void getPercentage_whenCacheEmpty_shouldFetchFromApi() {
        // Given
        BigDecimal expectedValue = new BigDecimal("0.2");
        
        when(cacheService.getCachedPercentage()).thenReturn(Mono.empty());
        when(externalApiClient.fetchPercentage()).thenReturn(Mono.just(expectedValue));
        when(cacheService.cachePercentage(expectedValue)).thenReturn(Mono.just(Boolean.TRUE));
        
        // When & Then
        StepVerifier.create(percentageService.getPercentage())
                .expectNext(expectedValue)
                .verifyComplete();
    }
}
