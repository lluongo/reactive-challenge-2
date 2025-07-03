package cl.tenpo.learning.reactive.tasks.task2.presentation.controller;

import cl.tenpo.learning.reactive.tasks.task2.application.port.CallHistoryService;
import cl.tenpo.learning.reactive.tasks.task2.domain.model.CallHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Controlador para manejar operaciones relacionadas con el historial de llamadas.
 */
@Slf4j
@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class HistoryController {
    
    private final CallHistoryService callHistoryService;
    
    /**
     * Obtiene el historial de llamadas a la API, con paginación.
     * 
     * @param page número de página (inicia en 0)
     * @param size tamaño de la página
     * @return Flux con el historial de llamadas
     */
    @GetMapping("${app.api.endpoints.history}")
    public Flux<CallHistory> getHistory(
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
