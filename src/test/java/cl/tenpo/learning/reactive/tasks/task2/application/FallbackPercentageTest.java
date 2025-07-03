package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.cache.PercentageCacheService;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.client.ExternalApiClient;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.event.RetryExhaustedEvent;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.exception.ServiceUnavailableException;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.retry.RetryStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test específico para verificar el comportamiento del servicio cuando el endpoint externo
 * no está disponible.
 */
@ExtendWith(MockitoExtension.class)
public class FallbackPercentageTest {

    @Mock
    private ExternalApiClient externalApiClient;

    @Mock
    private PercentageCacheService cacheService;

    @Mock
    private RetryStrategy retryStrategy;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ExternalPercentageService percentageService;

    @BeforeEach
    void setUp() {
        percentageService = new ExternalPercentageService(externalApiClient, cacheService, retryStrategy);

        // Configure retryStrategy to simulate event publishing 
        Retry customRetry = Retry.max(1)
            .onRetryExhaustedThrow((spec, signal) -> {
                // Crear un evento real en lugar de usar any()
                RetryExhaustedEvent event = new RetryExhaustedEvent(
                    Map.of("error", "Failed to fetch percentage: API Error")
                );
                eventPublisher.publishEvent(event);
                return new ServiceUnavailableException("Failed to fetch percentage");
            });
        
        when(retryStrategy.getRetrySpec(any())).thenReturn(customRetry);
    }

    @Test
    void testExternalServiceFailure_shouldThrowServiceUnavailableException() {
        // Given - El cache está vacío y el API externo falla
        when(cacheService.getCachedPercentage()).thenReturn(Mono.empty());
        when(externalApiClient.fetchPercentage()).thenReturn(Mono.error(new RuntimeException("API Error")));
        
        // When & Then - Verificar que se lanza ServiceUnavailableException
        StepVerifier.create(percentageService.getPercentage())
                .expectError(ServiceUnavailableException.class)
                .verify();
    }

    @Test
    void testUsesCachedValue_whenExternalServiceFails() {
        // Given - Existe un valor previo en caché
        BigDecimal cachedValue = new BigDecimal("0.07");
        when(cacheService.getCachedPercentage()).thenReturn(Mono.just(cachedValue));
        
        // When - Obtener el porcentaje
        Mono<BigDecimal> result = percentageService.getPercentage();
        
        // Then - Verificar que se usa el valor cacheado sin intentar llamar al API
        StepVerifier.create(result)
                .expectNext(cachedValue)
                .verifyComplete();
                
        // Verificar que NO se llamó al API externo
        verify(externalApiClient, never()).fetchPercentage();
        
        // Verificar que se intentó obtener de la caché
        verify(cacheService).getCachedPercentage();
    }
}
