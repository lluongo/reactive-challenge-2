package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.application.port.CallHistoryService;
import cl.tenpo.learning.reactive.tasks.task2.domain.model.CallHistory;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CallHistoryServiceImpl implements CallHistoryService {

    private static final Logger log = LoggerFactory.getLogger(CallHistoryServiceImpl.class);
    private static final Duration DATABASE_TIMEOUT = Duration.ofSeconds(10);
    private final CallHistoryRepository callHistoryRepository;
    private final PageableFactory pageableFactory;

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

    @Override
    public Mono<CallHistory> recordSuccessfulRequest(String endpoint, String method, String parameters, String response) {
        return createHistoryRecord(endpoint, method, parameters, response, true)
                .flatMap(callHistoryRepository::save)
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(DATABASE_TIMEOUT)
                .doOnSuccess(saved -> log.info("Successfully recorded request history: {}", saved.getId()))
                .doOnError(e -> log.error("Failed to record request history: {}", e.getMessage()))
                .onErrorResume(e -> {
                    log.warn("Suppressing error in history recording: {}", e.getMessage());
                    return Mono.empty();
                })
                .checkpoint("record-success-history");
    }

    @Override
    public Mono<CallHistory> recordFailedRequest(String endpoint, String method, String parameters, String error) {
        return createHistoryRecord(endpoint, method, parameters, error, false)
                .flatMap(callHistoryRepository::save)
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

    @Override
    public Mono<List<CallHistory>> getCallHistoryAsList(Pageable pageable) {
        return getCallHistory(pageable)
                .collectList()
                .doOnSuccess(history -> log.info("Retrieved {} call history records", history.size()))
                .doOnError(error -> log.error("Error retrieving call history list: {}", error.getMessage()));
    }

    @Override
    public Mono<List<CallHistory>> getCallHistoryFromParams(Integer page, Integer size) {
        Pageable pageable = pageableFactory.createPageable(page, size);
        log.info("Created pageable for call history: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        return getCallHistoryAsList(pageable);
    }

    private Mono<CallHistory> createHistoryRecord(String endpoint, String method, String parameters, String responseOrError, boolean successful) {
        return Mono.fromCallable(() -> {
            CallHistory.CallHistoryBuilder builder = CallHistory.builder()
                    .timestamp(LocalDateTime.now())
                    .endpoint(endpoint)
                    .method(method)
                    .parameters(parameters)
                    .successful(successful);

            if (successful) {
                builder.response(responseOrError);
            } else {
                builder.error(responseOrError);
            }

            return builder.build();
        });
    }
}
