package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.RedisConfig;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.TimeoutConfig;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.exception.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExternalPercentageServiceTest {

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

    private ExternalPercentageService percentageService;

    @BeforeEach
    void setUp() {
        when(timeoutConfig.getExternalApiTimeout()).thenReturn(Duration.ofSeconds(20));
        when(timeoutConfig.getCacheTimeout()).thenReturn(Duration.ofSeconds(5));
        
        percentageService = new ExternalPercentageService(webClient, reactiveRedisTemplate, eventPublisher, timeoutConfig);
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void getPercentage_whenCacheHasValue_shouldReturnCachedValue() {
        // Given - Un valor en la caché
        BigDecimal expectedValue = new BigDecimal("0.1");
        when(valueOps.get(RedisConfig.PERCENTAGE_KEY)).thenReturn(Mono.just(expectedValue));

        // When - Obtener el porcentaje
        Mono<BigDecimal> result = percentageService.getPercentage();
        
        // Then - Verificar que se usa el valor cacheado
        StepVerifier.create(result)
                .expectNext(expectedValue)
                .verifyComplete();
                
        // Verificar que se intentó obtener de la caché
        verify(valueOps).get(RedisConfig.PERCENTAGE_KEY);
        
        // Verificar que no se intentó obtener del API externo
        verify(webClient, never()).get();
    }

    @Test
    void getPercentage_whenCacheEmptyAndApiSucceeds_shouldFetchFromApiAndCache() {
        // Given
        String percentageStr = "0.15";
        BigDecimal expectedValue = new BigDecimal(percentageStr);
        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("percentage", percentageStr);
        
        when(valueOps.get(RedisConfig.PERCENTAGE_KEY)).thenReturn(Mono.empty());
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));
        when(valueOps.set(eq(RedisConfig.PERCENTAGE_KEY), eq(expectedValue), any(Duration.class)))
            .thenReturn(Mono.just(Boolean.TRUE));

        // When & Then
        StepVerifier.create(percentageService.getPercentage())
                .expectNext(expectedValue)
                .verifyComplete();
    }

    @Test
    void getPercentage_whenCacheEmptyAndApiFails_shouldThrowServiceUnavailableException() {
        // Given
        when(valueOps.get(RedisConfig.PERCENTAGE_KEY)).thenReturn(Mono.empty());
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(new RuntimeException("API Error")));
        
        // When & Then
        StepVerifier.create(percentageService.getPercentage())
                .expectError(ServiceUnavailableException.class)
                .verify();
    }

    @Test
    void getPercentage_whenCacheEmpty_shouldFetchFromApi() {
        // Given
        BigDecimal expectedValue = new BigDecimal("0.2");
        Map<String, String> apiResponse = new HashMap<>();
        apiResponse.put("percentage", "0.2");
        
        when(valueOps.get(RedisConfig.PERCENTAGE_KEY)).thenReturn(Mono.empty());
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/external-api/percentage")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));
        when(valueOps.set(eq(RedisConfig.PERCENTAGE_KEY), any(BigDecimal.class), any(Duration.class)))
                .thenReturn(Mono.just(Boolean.TRUE));
        
        // When & Then
        StepVerifier.create(percentageService.getPercentage())
                .expectNext(expectedValue)
                .verifyComplete();
    }
}
