package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.application.port.CallHistoryService;
import cl.tenpo.learning.reactive.tasks.task2.domain.model.CallHistory;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.PaginationConfig;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.TimeoutConfig;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.factory.PageableFactory;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.persistence.CallHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de historial de llamadas
 */
@Service
@RequiredArgsConstructor
public class CallHistoryServiceImpl implements CallHistoryService {

    private static final Logger log = LoggerFactory.getLogger(CallHistoryServiceImpl.class);
    
    private final CallHistoryRepository callHistoryRepository;
    private final PageableFactory pageableFactory;
    private final TimeoutConfig timeoutConfig;
    private final PaginationConfig paginationConfig;

    /**
     * Obtiene el historial de llamadas paginado
     */
    @Override
    public Flux<CallHistory> getCallHistory(Pageable pageable) {
        return callHistoryRepository.findAllBy(pageable)
                .timeout(timeoutConfig.getDatabaseOperation())
                .doOnNext(history -> log.debug("Historia de llamada recuperada: {}", history.getId()))
                .doOnError(error -> log.error("Error recuperando historial: {}", error.getMessage()))
                .onErrorResume(ex -> Flux.empty())
                .checkpoint("call-history-retrieval");
    }

    /**
     * Registra una solicitud exitosa
     */
    @Override
    public Mono<CallHistory> recordSuccessfulRequest(String endpoint, String method, String parameters, String response) {
        return Mono.just(buildHistoryRecord(endpoint, method, parameters, response, true))
                .flatMap(this::saveHistoryRecord)
                .checkpoint("record-success-history");
    }

    /**
     * Registra una solicitud fallida
     */
    @Override
    public Mono<CallHistory> recordFailedRequest(String endpoint, String method, String parameters, String error) {
        return Mono.just(buildHistoryRecord(endpoint, method, parameters, error, false))
                .flatMap(this::saveHistoryRecord)
                .checkpoint("record-failed-history");
    }

    /**
     * Guarda un registro de historial manejando errores
     */
    private Mono<CallHistory> saveHistoryRecord(CallHistory history) {
        return callHistoryRepository.save(history)
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(timeoutConfig.getDatabaseOperation())
                .doOnSuccess(saved -> log.info("Historial registrado: {} - {}", saved.getId(), saved.getEndpoint()))
                .doOnError(e -> log.error("Error guardando historial: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Obtiene el historial de llamadas como una lista
     */
    @Override
    public Mono<List<CallHistory>> getCallHistoryAsList(Pageable pageable) {
        return getCallHistory(pageable)
                .collectList()
                .doOnSuccess(history -> log.info("Recuperados {} registros de historial", history.size()));
    }

    /**
     * Obtiene el historial desde parámetros de página
     */
    @Override
    public Mono<List<CallHistory>> getCallHistoryFromParams(Integer page, Integer size) {
        return Mono.justOrEmpty(page)
                .zipWith(Mono.justOrEmpty(size).defaultIfEmpty(paginationConfig.getDefaultPageSize()))
                .map(tuple -> pageableFactory.createPageable(tuple.getT1(), tuple.getT2()))
                .defaultIfEmpty(pageableFactory.createPageable(
                        paginationConfig.getDefaultPage(), 
                        paginationConfig.getDefaultPageSize()))
                .doOnNext(pageable -> log.info("Creado pageable: página={}, tamaño={}", 
                        pageable.getPageNumber(), pageable.getPageSize()))
                .flatMap(this::getCallHistoryAsList);
    }

    /**
     * Crea un objeto de historial de llamadas
     */
    private CallHistory buildHistoryRecord(String endpoint, String method, String parameters, String responseOrError, boolean successful) {
        CallHistory.CallHistoryBuilder builder = CallHistory.builder()
                .timestamp(LocalDateTime.now())
                .endpoint(endpoint)
                .method(method)
                .parameters(parameters)
                .successful(successful);
                
        if (successful) {
            return builder.response(responseOrError).build();
        } else {
            return builder.error(responseOrError).build();
        }
    }
}
