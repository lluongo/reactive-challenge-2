package cl.tenpo.learning.reactive.tasks.task2.presentation.controller;

import cl.tenpo.learning.reactive.tasks.task2.application.port.CalculationService;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationRequest;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

/**
 * Controller REST para operaciones de cálculo.
 * Delega toda la lógica de negocio al servicio correspondiente.
 */
@Slf4j
@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class CalculationController {

    private final CalculationService calculationService;

    /**
     * Realiza un cálculo sumando dos números y aplicando un porcentaje
     * obtenido de un servicio externo o de la caché.
     * 
     * @param calculationRequest DTO con los números a calcular
     * @param exchange Intercambio de información HTTP
     * @return DTO con el resultado del cálculo
     */
    @PostMapping("${app.api.endpoints.calculation}")
    public Mono<CalculationResponse> calculate(
            @Valid @RequestBody CalculationRequest calculationRequest,
            ServerWebExchange exchange) {
        
        final String requestId = exchange.getRequest().getId();
        log.info("Received calculation request [{}]: {}", requestId, calculationRequest);
        
        return calculationService.processCalculationRequest(calculationRequest)
                .doOnSuccess(response -> log.info("Calculation request [{}] completed: {}", requestId, response))
                .doOnError(error -> log.error("Calculation request [{}] failed: {}", requestId, error.getMessage()));
    }
}
