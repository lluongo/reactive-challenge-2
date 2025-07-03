package cl.tenpo.learning.reactive.tasks.task2.presentation.controller;

import cl.tenpo.learning.reactive.tasks.task2.application.port.CalculationService;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationRequest;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * Controlador para manejar operaciones de cálculo.
 */
@Slf4j
@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class CalculationController {

    private final CalculationService calculationService;
    
    @Value("${app.api.endpoints.calculation}")
    private String calculationEndpoint;

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
        
        return Mono.deferContextual(ctx -> 
            calculationService.calculateWithPercentage(
                    calculationRequest.getNum1(), 
                    calculationRequest.getNum2()
            )
            .map(result -> new CalculationResponse(result, calculationRequest.getNum1(), calculationRequest.getNum2()))
            .doOnNext(response -> log.info("Calculation completed [{}] with result: {}", ctx.get("requestId"), response))
            .doOnError(e -> log.error("Error processing calculation [{}]: {}", ctx.get("requestId"), e.getMessage()))
        )
        .contextWrite(Context.of("requestId", requestId))
        .checkpoint("calculation-controller-response");
    }
}
