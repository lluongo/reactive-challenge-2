package cl.tenpo.learning.reactive.tasks.task2.presentation.controller;

import cl.tenpo.learning.reactive.tasks.task2.application.CalculationService;
import cl.tenpo.learning.reactive.tasks.task2.application.CallHistoryService;
import cl.tenpo.learning.reactive.tasks.task2.domain.model.CallHistory;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationRequest;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CalculationController {

    private final CalculationService calculationService;
    private final CallHistoryService callHistoryService;
    
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

    /**
     * Obtiene el historial de llamadas a la API, con paginación.
     * 
     * @param page número de página (inicia en 0)
     * @param size tamaño de la página
     * @return Flux con el historial de llamadas
     */
    @GetMapping("${app.api.endpoints.calculation}")
    public Flux<CallHistory> callHistory(
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Fetching call history, page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return callHistoryService.getCallHistory(pageable)
                .limitRate(100)  // Controla la tasa de elementos procesados
                .onBackpressureBuffer(1000)
                .doOnComplete(() -> log.info("Call history request completed"))
                .doOnError(e -> log.error("Error retrieving call history: {}", e.getMessage()))
                .checkpoint("call-history-response");
    }
}
