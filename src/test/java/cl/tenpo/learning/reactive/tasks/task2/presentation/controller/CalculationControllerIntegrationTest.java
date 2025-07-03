package cl.tenpo.learning.reactive.tasks.task2.presentation.controller;

import cl.tenpo.learning.reactive.tasks.task2.application.port.CalculationService;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationRequest;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests para CalculationController
 */
@ExtendWith(MockitoExtension.class)
public class CalculationControllerIntegrationTest {

    @Mock
    private CalculationService calculationService;

    @InjectMocks
    private CalculationController controller;

    @Mock
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        when(exchange.getRequest()).thenReturn(mock(org.springframework.http.server.reactive.ServerHttpRequest.class));
        when(exchange.getRequest().getId()).thenReturn("test-request-id");
    }

    @Test
    void calculate_shouldReturnCorrectResult() {
        // Given
        CalculationRequest request = new CalculationRequest(BigDecimal.valueOf(10), BigDecimal.valueOf(5));
        BigDecimal expectedResult = BigDecimal.valueOf(16.5);
        
        when(calculationService.calculateWithPercentage(any(), any()))
                .thenReturn(Mono.just(expectedResult));

        // When
        Mono<CalculationResponse> result = controller.calculate(request, exchange);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> 
                    response.getResult().equals(expectedResult) &&
                    response.getNum1().equals(BigDecimal.valueOf(10)) &&
                    response.getNum2().equals(BigDecimal.valueOf(5))
                )
                .verifyComplete();
    }

    @Test
    void calculate_withValidInputs_shouldProcessCorrectly() {
        // Given
        CalculationRequest request = new CalculationRequest(BigDecimal.valueOf(20), BigDecimal.valueOf(30));
        BigDecimal expectedResult = BigDecimal.valueOf(75.0);
        
        when(calculationService.calculateWithPercentage(any(), any()))
                .thenReturn(Mono.just(expectedResult));

        // When
        Mono<CalculationResponse> result = controller.calculate(request, exchange);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getResult().equals(expectedResult))
                .verifyComplete();
    }
}
