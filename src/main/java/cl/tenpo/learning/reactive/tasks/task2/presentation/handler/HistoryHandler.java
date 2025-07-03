package cl.tenpo.learning.reactive.tasks.task2.presentation.handler;

import cl.tenpo.learning.reactive.tasks.task2.application.port.CallHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Handler funcional para operaciones de historial.
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
        return Mono.fromCallable(() -> createPageable(request))
                .flatMap(pageable -> 
                    callHistoryService.getCallHistory(pageable)
                        .collectList()
                        .flatMap(history -> ServerResponse.ok().bodyValue(history))
                )
                .doOnError(error -> log.error("Error in history handler: {}", error.getMessage()))
                .onErrorResume(error -> 
                    ServerResponse.status(500)
                        .bodyValue("Error retrieving call history: " + error.getMessage())
                );
    }

    /**
     * Crea un objeto Pageable basado en los parámetros de la petición.
     */
    private Pageable createPageable(ServerRequest request) {
        int page = request.queryParam("page")
                .map(Integer::parseInt)
                .orElse(0);
        
        int size = request.queryParam("size")
                .map(Integer::parseInt)
                .orElse(10);
        
        return PageRequest.of(page, size);
    }
}
