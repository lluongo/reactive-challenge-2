package cl.tenpo.learning.reactive.tasks.task2.presentation.handler;

import cl.tenpo.learning.reactive.tasks.task2.application.port.CalculationService;
import cl.tenpo.learning.reactive.tasks.task2.presentation.dto.CalculationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalculationHandler {

    private final CalculationService calculationService;

    public Mono<ServerResponse> calculate(ServerRequest request) {
        return request.bodyToMono(CalculationRequest.class)
                .flatMap(calculationService::processCalculationRequest)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .doOnError(error -> log.error("Error in calculation handler: {}", error.getMessage()))
                .onErrorResume(error -> 
                    ServerResponse.badRequest()
                        .bodyValue("Invalid request: " + error.getMessage())
                );
    }
}
