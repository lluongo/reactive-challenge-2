package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.application.port.CallHistoryService;
import cl.tenpo.learning.reactive.tasks.task2.domain.model.CallHistory;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.persistence.CallHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Implementación del servicio para registrar y recuperar el historial de llamadas a la API.
 */
@Service
@RequiredArgsConstructor
public class CallHistoryServiceImpl implements CallHistoryService {

    private static final Logger log = LoggerFactory.getLogger(CallHistoryServiceImpl.class);
    private static final Duration DATABASE_TIMEOUT = Duration.ofSeconds(10);

    private final CallHistoryRepository callHistoryRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<CallHistory> getCallHistory(Pageable pageable) {
        return callHistoryRepository.findAllBy(pageable)
                .timeout(DATABASE_TIMEOUT)
                .doOnError(error -> log.error("Error retrieving call history: {}", error.getMessage()))
                .onErrorResume(e -> {
                    log.error("Error fetching call history, returning empty: {}", e.getMessage());
                    return Flux.empty();
                })
                .checkpoint("call-history-retrieval");
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
                .timeout(DATABASE_TIMEOUT)
                .doOnSuccess(saved -> log.info("Successfully recorded request history: {}", saved.getId()))
                .doOnError(error -> log.error("Failed to record request history: {}", error.getMessage()))
                .onErrorResume(e -> {
                    log.warn("Suppressing error in history recording: {}", e.getMessage());
                    return Mono.empty();
                })
                .checkpoint("record-success-history");
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
                .timeout(DATABASE_TIMEOUT)
                .doOnSuccess(saved -> log.info("Successfully recorded failed request history: {}", saved.getId()))
                .doOnError(e -> log.error("Failed to record request history: {}", e.getMessage()))
                .onErrorResume(e -> {
                    log.warn("Suppressing error in failed history recording: {}", e.getMessage());
                    return Mono.empty();
                })
                .checkpoint("record-failed-history");
    }
}
