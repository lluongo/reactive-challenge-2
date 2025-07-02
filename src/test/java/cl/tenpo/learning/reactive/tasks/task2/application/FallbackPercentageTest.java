package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.RedisConfig;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.TimeoutConfig;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.event.RetryExhaustedEvent;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.exception.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test específico para verificar el comportamiento del servicio cuando el endpoint externo
 * no está disponible.
 */
@ExtendWith(MockitoExtension.class)
public class FallbackPercentageTest {

    @Mock
    private WebClient webClient;

    @Mock
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    
    @Mock
    private ReactiveValueOperations<String, Object> valueOps;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TimeoutConfig timeoutConfig;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Captor
    private ArgumentCaptor<Object> eventCaptor;

    private ExternalPercentageService percentageService;
    private static final String PERCENTAGE_PATH = "/external-api/percentage";

    @BeforeEach
    void setUp() {
        when(timeoutConfig.getExternalApiTimeout()).thenReturn(Duration.ofSeconds(20));
        when(timeoutConfig.getCacheTimeout()).thenReturn(Duration.ofSeconds(5));
        
        percentageService = new ExternalPercentageService(webClient, reactiveRedisTemplate, eventPublisher, timeoutConfig);
        ReflectionTestUtils.setField(percentageService, "percentagePath", PERCENTAGE_PATH);
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void testExternalServiceFailure_shouldThrowServiceUnavailableException() {
        // Given - El cache está vacío y el API externo falla
        when(valueOps.get(RedisConfig.PERCENTAGE_KEY)).thenReturn(Mono.empty());
        
        // Configurar que el API externo falle
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq(PERCENTAGE_PATH))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(
            Mono.error(WebClientResponseException.create(500, "Server Error", null, null, null))
        );
            
        // When & Then - Verificar que se lanza ServiceUnavailableException
        StepVerifier.create(percentageService.getPercentage())
                .expectError(ServiceUnavailableException.class)
                .verify();
                
        // Verificar que se publicó un evento
        verify(eventPublisher).publishEvent(any(RetryExhaustedEvent.class));
    }

    @Test
    void testUsesCachedValue_whenExternalServiceFails() {
        // Given - Existe un valor previo en caché
        BigDecimal cachedValue = new BigDecimal("0.07");
        when(valueOps.get(RedisConfig.PERCENTAGE_KEY)).thenReturn(Mono.just(cachedValue));
        
        // When - Obtener el porcentaje
        Mono<BigDecimal> result = percentageService.getPercentage();
        
        // Then - Verificar que se usa el valor cacheado sin intentar llamar al API
        StepVerifier.create(result)
                .expectNext(cachedValue)
                .verifyComplete();
                
        // Verificar que NO se llamó al API externo
        verify(webClient, never()).get();
        
        // Verificar que se intentó obtener de la caché
        verify(valueOps).get(RedisConfig.PERCENTAGE_KEY);
    }
}
