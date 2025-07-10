package cl.tenpo.learning.reactive.tasks.task2.application;

import cl.tenpo.learning.reactive.tasks.task2.application.port.CallHistoryService;
import cl.tenpo.learning.reactive.tasks.task2.domain.model.CallHistory;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.PaginationConfig;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.TimeoutConfig;
import cl.tenpo.learning.reactive.tasks.task2.infrastructure.exception.UnauthorizedException;
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

@Service
@RequiredArgsConstructor
public class CallHistoryServiceImpl implements CallHistoryService {

    private static final Logger log = LoggerFactory.getLogger(CallHistoryServiceImpl.class);
    
    private final CallHistoryRepository callHistoryRepository;
    private final PageableFactory pageableFactory;
    private final TimeoutConfig timeoutConfig;
    private final PaginationConfig paginationConfig;
    private final AuthorizedUserService userService;

    @Override
    public Flux<CallHistory> getCallHistory(Pageable pageable) {
        return callHistoryRepository.findAllBy(pageable)
                .timeout(timeoutConfig.getDatabaseOperation())
                .doOnNext(history -> log.debug("Historia de llamada recuperada: {}", history.getId()))
                .doOnError(error -> log.error("Error recuperando historial: {}", error.getMessage()))
                .onErrorResume(ex -> Flux.empty())
                .checkpoint("call-history-retrieval");
    }

    @Override
    public Flux<CallHistory> getHistoryForAuthorizedUser(String username, Pageable pageable) {
        return userService.isUserAuthorized(username)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new UnauthorizedException("Usuario no autorizado para acceder al historial")))
                .flatMapMany(authorized -> getCallHistory(pageable))
                .limitRate(100)
                .onBackpressureBuffer(1000)
                .doOnComplete(() -> log.info("Historial completado para usuario: {}", username))
                .checkpoint("authorized-history-" + username);
    }

    @Override
    public Mono<List<CallHistory>> getCallHistoryAsList(Pageable pageable) {
        return getCallHistory(pageable)
                .collectList()
                .doOnSuccess(list -> log.debug("Recuperados {} registros de historial", list.size()));
    }

    @Override
    public Mono<List<CallHistory>> getCallHistoryFromParams(Integer page, Integer size) {
        return createPageableFromParams(page, size)
                .flatMap(this::getCallHistoryAsList)
                .checkpoint("history-from-params");
    }
    
    private Mono<Pageable> createPageableFromParams(Integer page, Integer size) {
        return Mono.justOrEmpty(page)
                .zipWith(Mono.justOrEmpty(size).defaultIfEmpty(paginationConfig.getDefaultPageSize()))
                .map(tuple -> pageableFactory.createPageable(tuple.getT1(), tuple.getT2()))
                .defaultIfEmpty(pageableFactory.createPageable(
                        paginationConfig.getDefaultPage(), 
                        paginationConfig.getDefaultPageSize()))
                .doOnNext(pageable -> log.info("Pageable creado: página={}, tamaño={}", 
                        pageable.getPageNumber(), pageable.getPageSize()));
    }

    @Override
    public Mono<CallHistory> recordSuccessfulRequest(String endpoint, String method, String parameters, String response) {
        return Mono.just(buildHistoryRecord(endpoint, method, parameters, response, true))
                .flatMap(this::saveHistoryRecord)
                .checkpoint("record-success-history");
    }

    @Override
    public Mono<CallHistory> recordFailedRequest(String endpoint, String method, String parameters, String error) {
        return Mono.just(buildHistoryRecord(endpoint, method, parameters, error, false))
                .flatMap(this::saveHistoryRecord)
                .checkpoint("record-failed-history");
    }

    private Mono<CallHistory> saveHistoryRecord(CallHistory history) {
        return callHistoryRepository.save(history)
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(timeoutConfig.getDatabaseOperation())
                .doOnSuccess(saved -> log.info("Historial registrado: {} - {}", saved.getId(), saved.getEndpoint()))
                .doOnError(e -> log.error("Error guardando historial: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    private CallHistory buildHistoryRecord(String endpoint, String method, String parameters, String responseOrError, boolean successful) {
        return CallHistory.builder()
                .timestamp(LocalDateTime.now())
                .endpoint(endpoint)
                .method(method)
                .parameters(parameters)
                .successful(successful)
                .response(successful ? responseOrError : null)
                .error(!successful ? responseOrError : null)
                .build();
    }
}
