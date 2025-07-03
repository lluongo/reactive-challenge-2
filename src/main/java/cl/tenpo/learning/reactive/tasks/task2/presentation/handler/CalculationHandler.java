package cl.tenpo.learning.reactive.tasks.task2.presentation.handler;

import cl.tenpo.learning.reactive.tasks.task2.application.port.CalculationService;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationRequest;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.time.Instant;
import java.util.UUID;

/**
 * Handler funcional para operaciones de cálculo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CalculationHandler {

    private final CalculationService calculationService;

    /**
     * Maneja las peticiones de cálculo usando programación funcional.
     */
    public Mono<ServerResponse> calculate(ServerRequest request) {
        return request.bodyToMono(CalculationRequest.class)
                .flatMap(this::validateRequest)
                .flatMap(this::processCalculation)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .doOnError(error -> log.error("Error in calculation handler: {}", error.getMessage()))
                .onErrorResume(this::handleErrorReactively)
                .contextWrite(this::enrichContext);
    }

    /**
     * Procesa el cálculo abstrayendo la lógica en una función separada.
     */
    private Mono<CalculationResponse> processCalculation(CalculationRequest calculationRequest) {
        return calculationService.calculateWithPercentage(
                calculationRequest.getNum1(), 
                calculationRequest.getNum2()
            )
            .map(result -> new CalculationResponse(result, calculationRequest.getNum1(), calculationRequest.getNum2()));
    }

    /**
     * Valida la petición de cálculo usando operadores reactivos.
     */
    private Mono<CalculationRequest> validateRequest(CalculationRequest request) {
        return Mono.just(request)
                .filter(req -> req.getNum1() != null && req.getNum2() != null)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Both numbers must be provided")));
    }

    /**
     * Maneja errores usando operadores reactivos con predicates.
     */
    private Mono<ServerResponse> handleErrorReactively(Throwable error) {
        log.error("Error processing calculation request: {}", error.getMessage());
        
        return Mono.just(error)
                .filter(IllegalArgumentException.class::isInstance)
                .cast(IllegalArgumentException.class)
                .flatMap(ex -> ServerResponse.badRequest()
                        .bodyValue("Invalid request: " + ex.getMessage()))
                .switchIfEmpty(
                    ServerResponse.status(500)
                        .bodyValue("Internal server error occurred")
                );
    }

    /**
     * Enriquece el contexto reactivo con información de la petición.
     */
    private Context enrichContext(Context ctx) {
        return ctx
                .put("requestId", UUID.randomUUID().toString())
                .put("startTime", Instant.now());
    }
}
