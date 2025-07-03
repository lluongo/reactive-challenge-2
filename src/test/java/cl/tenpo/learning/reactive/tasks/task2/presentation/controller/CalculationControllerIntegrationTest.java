package cl.tenpo.learning.reactive.tasks.task2.presentation.controller;

import cl.tenpo.learning.reactive.tasks.task2.application.port.CalculationService;
import cl.tenpo.learning.reactive.tasks.task2.application.port.CallHistoryService;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationRequest;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = CalculationController.class)
public class CalculationControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CalculationService calculationService;

    @MockBean
    private CallHistoryService callHistoryService;

    @Test
    void calculate_happyPath_shouldReturnCorrectResult() {
        // Given
        CalculationRequest request = new CalculationRequest(new BigDecimal("10.0"), new BigDecimal("5.0"));
        BigDecimal result = new BigDecimal("16.5"); // (10 + 5) + (10 + 5) * 0.1 = 16.5
        CalculationResponse expectedResponse = new CalculationResponse(result, request.getNum1(), request.getNum2());
        
        when(calculationService.calculateWithPercentage(eq(request.getNum1()), eq(request.getNum2())))
                .thenReturn(Mono.just(result));
        
        when(callHistoryService.getCallHistory(any(Pageable.class)))
                .thenReturn(Flux.empty());

        // When & Then
        webTestClient.post()
                .uri("/v1/calculation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CalculationResponse.class)
                .isEqualTo(expectedResponse);
    }

    @Test
    void calculate_withInvalidInput_shouldReturnBadRequest() {
        // Test with invalid input (null values)
        CalculationRequest request = new CalculationRequest(null, null);
        
        webTestClient.post()
                .uri("/v1/calculation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
