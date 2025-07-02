package cl.tenpo.learning.reactive.tasks.task2.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CalculationServiceTest {

    @Mock
    private ExternalPercentageService externalPercentageService;

    @InjectMocks
    private CalculationService calculationService;

    private BigDecimal num1;
    private BigDecimal num2;

    @BeforeEach
    void setUp() {
        // Setup calculation request
        num1 = new BigDecimal("10.0");
        num2 = new BigDecimal("5.0");
    }

    @Test
    void calculateWithPercentage_withValidPercentage_shouldApplyPercentageCorrectly() {
        // Given
        BigDecimal percentage = new BigDecimal("0.1"); // 10%
        when(externalPercentageService.getPercentage()).thenReturn(Mono.just(percentage));

        // When & Then
        StepVerifier.create(calculationService.calculateWithPercentage(num1, num2))
                .expectNextMatches(result -> 
                    result.compareTo(new BigDecimal("16.5")) == 0 // (10 + 5) + (10 + 5) * 0.1 = 15 + 1.5 = 16.5
                )
                .verifyComplete();
    }

    @Test
    void calculateWithPercentage_withZeroPercentage_shouldReturnSumOnly() {
        // Given
        BigDecimal percentage = new BigDecimal("0.0"); // 0%
        when(externalPercentageService.getPercentage()).thenReturn(Mono.just(percentage));

        // When & Then
        StepVerifier.create(calculationService.calculateWithPercentage(num1, num2))
                .expectNextMatches(result -> 
                    result.compareTo(new BigDecimal("15.0")) == 0 // (10 + 5) + (10 + 5) * 0 = 15
                )
                .verifyComplete();
    }

    @Test
    void calculateWithPercentage_withErrorFromPercentageService_shouldPropagateError() {
        // Given
        RuntimeException expectedException = new RuntimeException("Test exception");
        when(externalPercentageService.getPercentage()).thenReturn(Mono.error(expectedException));

        // When & Then
        StepVerifier.create(calculationService.calculateWithPercentage(num1, num2))
                .expectErrorMatches(error -> error == expectedException)
                .verify();
    }
}
