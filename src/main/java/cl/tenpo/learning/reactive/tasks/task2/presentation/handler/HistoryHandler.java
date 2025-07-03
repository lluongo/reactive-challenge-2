package cl.tenpo.learning.reactive.tasks.task2.presentation.handler;

import cl.tenpo.learning.reactive.tasks.task2.application.port.CallHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Handler funcional para operaciones de historial de llamadas.
 * Delega toda la lógica de negocio al servicio correspondiente.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HistoryHandler {

    private final CallHistoryService callHistoryService;

    /**
     * Maneja las peticiones de historial usando programación funcional.
     */
    public Mono<ServerResponse> getHistory(ServerRequest request) {
        Integer page = request.queryParam("page").map(Integer::parseInt).orElse(null);
        Integer size = request.queryParam("size").map(Integer::parseInt).orElse(null);
        
        return callHistoryService.getCallHistoryFromParams(page, size)
                .flatMap(history -> ServerResponse.ok().bodyValue(history))
                .doOnError(error -> log.error("Error in history handler: {}", error.getMessage()))
                .onErrorResume(error -> 
                    ServerResponse.status(500)
                        .bodyValue("Error retrieving call history: " + error.getMessage())
                );
    }
}
