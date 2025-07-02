package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.domain.model.CallHistory;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.TimeoutConfig;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.persistence.CallHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

/**
 * Servicio para registrar y recuperar el historial de llamadas a la API.
 * El registro se realiza de manera asíncrona para no afectar el tiempo de respuesta.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallHistoryService {

    private final CallHistoryRepository callHistoryRepository;
    private final TimeoutConfig timeoutConfig;

    /**
     * Obtiene el historial de llamadas a la API con paginación.
     * 
     * @param pageable configuración de paginación
     * @return Flux de registros de historial
     */
    public Flux<CallHistory> getCallHistory(Pageable pageable) {
        return callHistoryRepository.findAllBy(pageable)
                .timeout(timeoutConfig.getDatabaseTimeout())
                .doOnError(error -> log.error("Error retrieving call history: {}", error.getMessage()))
                .onErrorResume(e -> {
                    log.error("Error fetching call history, returning empty: {}", e.getMessage());
                    return Flux.empty();
                })
                .checkpoint("call-history-retrieval");
    }

    /**
     * Registra una solicitud exitosa en el historial.
     * Este método es asíncrono y no bloquea el flujo principal.
     */
    public Mono<CallHistory> recordSuccessfulRequest(String endpoint, String method, String parameters, String response) {
        CallHistory history = CallHistory.builder()
                .timestamp(LocalDateTime.now())
                .endpoint(endpoint)
                .method(method)
                .parameters(parameters)
                .response(response)
                .successful(true)
                .build();

        // Run asynchronously in a separate thread to avoid blocking or impacting response time
        return Mono.defer(() -> callHistoryRepository.save(history))
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(timeoutConfig.getDatabaseTimeout())
                .doOnSuccess(saved -> log.info("Successfully recorded request history: {}", saved.getId()))
                .doOnError(error -> log.error("Failed to record request history: {}", error.getMessage()))
                .onErrorResume(e -> {
                    log.warn("Suppressing error in history recording: {}", e.getMessage());
                    return Mono.empty();
                })
                .checkpoint("record-success-history");
    }

    /**
     * Registra una solicitud fallida en el historial.
     * Este método es asíncrono y no bloquea el flujo principal.
     */
    public Mono<CallHistory> recordFailedRequest(String endpoint, String method, String parameters, String error) {
        CallHistory history = CallHistory.builder()
                .timestamp(LocalDateTime.now())
                .endpoint(endpoint)
                .method(method)
                .parameters(parameters)
                .error(error)
                .successful(false)
                .build();

        // Run asynchronously in a separate thread to avoid blocking or impacting response time
        return Mono.defer(() -> callHistoryRepository.save(history))
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(timeoutConfig.getDatabaseTimeout())
                .doOnSuccess(saved -> log.info("Successfully recorded failed request history: {}", saved.getId()))
                .doOnError(e -> log.error("Failed to record request history: {}", e.getMessage()))
                .onErrorResume(e -> {
                    log.warn("Suppressing error in failed history recording: {}", e.getMessage());
                    return Mono.empty();
                })
                .checkpoint("record-failed-history");
    }
}
